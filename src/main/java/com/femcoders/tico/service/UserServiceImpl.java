package com.femcoders.tico.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivationService activationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TicketRepository ticketRepository;

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
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserResponseDTO dto = userMapper.toResponseDTO(user);
                    int openTickets = (int) ticketRepository.findByCreatedById(user.getId())
                            .stream()
                            .filter(t -> t.getStatus() != com.femcoders.tico.enums.TicketStatus.CLOSED)
                            .count();
                    return new UserResponseDTO(
                            dto.id(), dto.name(), dto.email(), dto.roles(),
                            dto.isActive(), openTickets, dto.createdAt());
                })
                .toList();
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

        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    @Override
    public List<UserResponseDTO> getAllAdmins() {
        return userRepository.findByRolesContaining(UserRole.ADMIN)
                .stream().map(userMapper::toResponseDTO).toList();
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UpdateUserReqDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        userMapper.updateEntity(dto, user);
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        return userMapper.toResponseDTO(userRepository.save(user));
    }
}
