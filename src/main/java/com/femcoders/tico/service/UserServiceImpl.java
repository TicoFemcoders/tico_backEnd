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

import com.femcoders.tico.dto.request.AdminCreateUserReqDTO;
import com.femcoders.tico.dto.request.UpdateUserReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
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

    @Override
    @Transactional
    public UserResponseDTO createUser(AdminCreateUserReqDTO userDto) {
        User user = userMapper.toEntity(userDto);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        User savedUser = userRepository.save(user);

        String code = activationService.generateCodeAndSaveToken(savedUser, TokenType.ACTIVATION);

        try {
            emailService.sendActivationEmail(savedUser.getEmail(), savedUser.getName(), code);
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de activación a {}: {}", savedUser.getEmail(), e.getMessage());
        }

        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        Map<Long, Long> openCounts = ticketRepository.countOpenTicketsPerUser()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));

        return userRepository.findAll(pageable)
                .map(user -> {
                    UserResponseDTO base = userMapper.toResponseDTO(user);
                    long open = openCounts.getOrDefault(user.getId(), 0L);
                    return new UserResponseDTO(
                            base.id(), base.name(), base.email(),
                            base.roles(), base.isActive(), open, base.createdAt());
                });
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, String reassignEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        List<Ticket> activeTickets = ticketRepository.findByCreatedById(userId)
                .stream()
                .filter(t -> t.getStatus() != com.femcoders.tico.enums.TicketStatus.CLOSED)
                .toList();

        if (!activeTickets.isEmpty()) {
            if (reassignEmail == null || reassignEmail.isBlank()) {
                throw new ConflictException(
                        "El usuario tiene " + activeTickets.size()
                                + " tickets abiertos. Proporciona un email para reasignarlos.");
            }
            User newOwner = userRepository.findByEmail(reassignEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", reassignEmail));

            activeTickets.forEach(t -> t.setCreatedBy(newOwner));
            ticketRepository.saveAll(activeTickets);
        }

        ticketRepository.unassignOpenTicketsByAdmin(userId);
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    @Override
    public Page<UserResponseDTO> getAllAdmins(Pageable pageable) {
        return userRepository.findByRolesContaining(UserRole.ADMIN, pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UpdateUserReqDTO dto) {
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
    public UserResponseDTO toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        return userMapper.toResponseDTO(userRepository.save(user));
    }
}
