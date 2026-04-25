package com.femcoders.tico.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.femcoders.tico.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.femcoders.tico.dto.request.AdminCreateUserRequest;
import com.femcoders.tico.dto.request.UpdateUserRequest;
import com.femcoders.tico.dto.response.UserResponse;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.ConflictException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.UserMapper;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;
import com.femcoders.tico.security.UserDetail;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivationService activationService;
    private final EmailService emailService;
    private final TicketRepository ticketRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest userDto) {
        User user = userMapper.toEntity(userDto);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        User savedUser = userRepository.save(user);

        String code = activationService.generateCodeAndSaveToken(savedUser, TokenType.ACTIVATION);

        try {
            emailService.sendActivationEmail(savedUser.getEmail(), savedUser.getName(), code);
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de activación a {}: {}", maskEmail(savedUser.getEmail()),
                    e.getMessage());
        }

        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Map<Long, Long> createdCounts = ticketRepository.countOpenTicketsPerUser()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));

        Map<Long, Long> assignedCounts = ticketRepository.countOpenTicketsPerAdmin()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));

        return userRepository.findAll(pageable)
                .map(user -> {
                    UserResponse base = userMapper.toResponseDTO(user);
                    long open = user.getRoles().contains(UserRole.ADMIN)
                            ? assignedCounts.getOrDefault(user.getId(), 0L)
                            : createdCounts.getOrDefault(user.getId(), 0L);

                    return new UserResponse(
                            base.id(), base.name(), base.email(),
                            base.roles(), base.isActive(), open, base.createdAt());
                });
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, String reassignEmail) {
        User currentUser = authService.getAuthenticatedUser();

        if (currentUser.getId().equals(userId)) {
            throw new BadRequestException("No puedes eliminarte a ti mismo");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (user.getRoles().contains(UserRole.ADMIN)
                && userRepository.countByRolesContaining(UserRole.ADMIN) <= 1) {
            throw new ConflictException("No puedes eliminar al único administrador del sistema");
        }

        // createdBy tickets: silently transferred to the admin doing the deletion (avoids FK violation)
        List<Ticket> createdByUser = ticketRepository.findByCreatedById(userId);
        createdByUser.forEach(t -> t.setCreatedBy(currentUser));

        // assignedTo tickets: only relevant for admins, requires explicit reassignment
        if (user.getRoles().contains(UserRole.ADMIN)) {
            List<Ticket> assignedToUser = ticketRepository.findByAssignedToIdAndStatusNot(userId,
                    TicketStatus.CLOSED);
            if (!assignedToUser.isEmpty()) {
                if (reassignEmail == null || reassignEmail.isBlank()) {
                    throw new ConflictException(
                            "El administrador tiene " + assignedToUser.size()
                                    + " tickets asignados activos. Proporciona un email para reasignarlos.");
                }
                User newOwner = userRepository.findByEmail(reassignEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", reassignEmail));
                assignedToUser.forEach(t -> {
                    t.setAssignedTo(newOwner);
                    notificationService.create(
                            t.getId(),
                            currentUser,
                            newOwner.getId(),
                            "Se te ha asignado el ticket: " + t.getEmailSubject());
                });
            }
        }

        userRepository.delete(user);

    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return "***";
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    @Override
    public Page<UserResponse> getAllAdmins(Pageable pageable) {
        return userRepository.findByRolesContaining(UserRole.ADMIN, pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest dto) {
        User currentUser = authService.getAuthenticatedUser();

        if (currentUser.getId().equals(id) && !dto.roles().contains(UserRole.ADMIN)) {
            throw new BadRequestException("No puedes eliminarte el rol de administrador");
        }

        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (target.getRoles().contains(UserRole.ADMIN)
                && !dto.roles().contains(UserRole.ADMIN)
                && userRepository.countByRolesContaining(UserRole.ADMIN) <= 1) {
            throw new ConflictException("No puedes degradar al único administrador del sistema");
        }

        userMapper.updateEntity(dto, target);
        return userMapper.toResponseDTO(userRepository.save(target));
    }

    @Override
    @Transactional
    public UserResponse toggleUserActive(Long id, String reassignEmail) {
        User currentUser = authService.getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        boolean isCurrentlyActive = Boolean.TRUE.equals(user.getIsActive());

        if (currentUser.getId().equals(id) && isCurrentlyActive) {
            throw new BadRequestException("No puedes desactivar tu propia cuenta");
        }

        if (user.getRoles().contains(UserRole.ADMIN)
                && isCurrentlyActive
                && userRepository.countByRolesContaining(UserRole.ADMIN) <= 1) {
            throw new ConflictException("No puedes desactivar al único administrador activo del sistema");
        }

        if (isCurrentlyActive && user.getRoles().contains(UserRole.ADMIN)) {
            List<Ticket> assignedTickets = ticketRepository.findByAssignedToIdAndStatusNot(id, TicketStatus.CLOSED);
            if (!assignedTickets.isEmpty()) {
                if (reassignEmail == null || reassignEmail.isBlank()) {
                    throw new ConflictException(
                            "El administrador tiene " + assignedTickets.size()
                                    + " tickets asignados activos. Proporciona un email para reasignarlos.");
                }
                User newOwner = userRepository.findByEmail(reassignEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", reassignEmail));
                assignedTickets.forEach(t -> {
                    t.setAssignedTo(newOwner);
                    notificationService.create(
                            t.getId(),
                            currentUser,
                            newOwner.getId(),
                            "Se te ha asignado el ticket: " + t.getEmailSubject());
                });
            }
        }

        user.setIsActive(!isCurrentlyActive);
        return userMapper.toResponseDTO(userRepository.save(user));
    }
}
