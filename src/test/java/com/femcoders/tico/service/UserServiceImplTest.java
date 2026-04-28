package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.AdminCreateUserRequest;
import com.femcoders.tico.dto.request.UpdateUserRequest;
import com.femcoders.tico.dto.response.UserResponse;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ConflictException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.UserMapper;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ActivationService activationService;

    @Mock
    private EmailService emailService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AuthService authService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse mockResponse;
    private AdminCreateUserRequest createRequest;


    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Ana García");
        user.setEmail("ana@tico.com");
        user.setIsActive(false);

        mockResponse = new UserResponse(1L, "Ana García", "ana@tico.com", Set.of("EMPLOYEE"), false, 0L, LocalDateTime.now());
        createRequest = new AdminCreateUserRequest("Ana García", "ana@tico.com", Set.of(com.femcoders.tico.enums.UserRole.EMPLOYEE));
    }

    @Test
    void createUser_whenAllIsOk_generatesTokenAndSendsEmail() {
        when(userMapper.toEntity(createRequest)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_temp_password");
        when(userRepository.save(user)).thenReturn(user);
        when(activationService.generateCodeAndSaveToken(user, TokenType.ACTIVATION)).thenReturn("ABC123");
        when(userMapper.toResponseDTO(user)).thenReturn(mockResponse);

        UserResponse result = userService.createUser(createRequest);

        assertNotNull(result);

        verify(activationService).generateCodeAndSaveToken(user, TokenType.ACTIVATION);
        verify(emailService).sendActivationEmail("ana@tico.com", "Ana García", "ABC123");
    }

    @Test
    void createUser_whenEmailFails_stillCreatesUser() {
        when(userMapper.toEntity(createRequest)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_temp_password");
        when(userRepository.save(user)).thenReturn(user);
        when(activationService.generateCodeAndSaveToken(user, TokenType.ACTIVATION)).thenReturn("ABC123");

        doThrow(new RuntimeException("SMTP caído")).when(emailService)
                .sendActivationEmail(anyString(), anyString(), anyString());

        when(userMapper.toResponseDTO(user)).thenReturn(mockResponse);

        UserResponse result = assertDoesNotThrow(() -> userService.createUser(createRequest));

        assertNotNull(result);

        verify(userRepository).save(user);
    }

    @Test
    void getUserById_whenUserExists_returnsUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(mockResponse);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("Ana García", result.name());
        assertEquals("ana@tico.com", result.email());
}

    @Test
    void getUserById_whenUserNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void updateUser_whenAdminRemovesOwnAdminRole_throwsBadRequestException() {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRoles(Set.of(UserRole.ADMIN));

        UpdateUserRequest dto = new UpdateUserRequest("Ana García", "ana@tico.com", Set.of(UserRole.EMPLOYEE));
        when(authService.getAuthenticatedUser()).thenReturn(adminUser);

        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, dto));
    }

    @Test
    void updateUser_whenDegradingLastAdmin_throwsConflictException() {
        User currentAdmin = new User();
        currentAdmin.setId(2L);
        currentAdmin.setRoles(Set.of(UserRole.ADMIN));

        User targetAdmin = new User();
        targetAdmin.setId(1L);
        targetAdmin.setRoles(Set.of(UserRole.ADMIN));

        UpdateUserRequest dto = new UpdateUserRequest("Ana García", "ana@tico.com", Set.of(UserRole.EMPLOYEE));

        when(authService.getAuthenticatedUser()).thenReturn(currentAdmin);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRolesContaining(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, dto));
    }

    @Test
    void updateUser_whenAllIsOk_returnsUpdatedUser() {
        User currentAdmin = new User();
        currentAdmin.setId(2L);
        currentAdmin.setRoles(Set.of(UserRole.ADMIN));

        UpdateUserRequest dto = new UpdateUserRequest("Ana García", "ana@tico.com", Set.of(UserRole.EMPLOYEE));

        when(authService.getAuthenticatedUser()).thenReturn(currentAdmin);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(mockResponse);

        UserResponse result = userService.updateUser(1L, dto);

        assertNotNull(result);
        verify(userMapper).updateEntity(dto, user);
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserActive_whenDeactivatingOwnAccount_throwsBadRequestException() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setIsActive(true);
        currentUser.setRoles(Set.of(UserRole.EMPLOYEE));

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        assertThrows(BadRequestException.class, () -> userService.toggleUserActive(1L, null));
    }

    @Test
    void toggleUserActive_whenDeactivatingLastAdmin_throwsConflictException() {
        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setIsActive(true);
        currentUser.setRoles(Set.of(UserRole.ADMIN));

        User targetAdmin = new User();
        targetAdmin.setId(1L);
        targetAdmin.setIsActive(true);
        targetAdmin.setRoles(Set.of(UserRole.ADMIN));

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRolesContaining(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> userService.toggleUserActive(1L, null));
    }

    @Test
    void toggleUserActive_whenActivatingInactiveUser_returnsUpdatedUser() {
        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setIsActive(true);
        currentUser.setRoles(Set.of(UserRole.ADMIN));

        user.setIsActive(false); // el user del @BeforeEach
        user.setRoles(Set.of(UserRole.EMPLOYEE));

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(mockResponse);

        UserResponse result = userService.toggleUserActive(1L, null);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserActive_whenAdminHasActiveTickets_andNoReassignEmail_throwsConflictException() {
        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setIsActive(true);
        currentUser.setRoles(Set.of(UserRole.ADMIN));

        User targetAdmin = new User();
        targetAdmin.setId(1L);
        targetAdmin.setIsActive(true);
        targetAdmin.setRoles(Set.of(UserRole.ADMIN));

        Ticket activeTicket = new Ticket();
        activeTicket.setId(1L);

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRolesContaining(UserRole.ADMIN)).thenReturn(2L);
        when(ticketRepository.findByAssignedToIdAndStatusNot(1L, TicketStatus.CLOSED))
                .thenReturn(List.of(activeTicket));
        when(ticketRepository.findByCreatedByIdAndStatusNot(1L, TicketStatus.CLOSED))
                .thenReturn(List.of());

        assertThrows(ConflictException.class,
                () -> userService.toggleUserActive(1L, null));
    }

    @Test
    void toggleUserActive_whenAdminHasActiveTickets_andReassignEmailProvided_reassignsAndDeactivates() {
        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setIsActive(true);
        currentUser.setRoles(Set.of(UserRole.ADMIN));

        User targetAdmin = new User();
        targetAdmin.setId(1L);
        targetAdmin.setIsActive(true);
        targetAdmin.setRoles(Set.of(UserRole.ADMIN));

        User newOwner = new User();
        newOwner.setId(3L);
        newOwner.setIsActive(true);
        newOwner.setRoles(Set.of(UserRole.ADMIN));

        Ticket activeTicket = new Ticket();
        activeTicket.setId(1L);
        activeTicket.setEmailSubject("Ticket test");

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRolesContaining(UserRole.ADMIN)).thenReturn(2L);
        when(ticketRepository.findByAssignedToIdAndStatusNot(1L, TicketStatus.CLOSED))
                .thenReturn(List.of(activeTicket));
        when(ticketRepository.findByCreatedByIdAndStatusNot(1L, TicketStatus.CLOSED))
                .thenReturn(List.of());
        when(userRepository.findByEmail("newowner@test.com")).thenReturn(Optional.of(newOwner));
        when(userRepository.save(targetAdmin)).thenReturn(targetAdmin);
        when(userMapper.toResponseDTO(targetAdmin)).thenReturn(mockResponse);

        UserResponse result = userService.toggleUserActive(1L, "newowner@test.com");

        assertNotNull(result);
        assertEquals(newOwner, activeTicket.getAssignedTo());
        verify(userRepository).save(targetAdmin);
    }

    @Test
    void toggleUserActive_whenUserNotFound_throwsResourceNotFoundException() {
        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setIsActive(true);
        currentUser.setRoles(Set.of(UserRole.ADMIN));

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.toggleUserActive(99L, null));
    }

    @Test
    void loadUserByUsername_whenEmailExists_returnsUserDetails() {
        when(userRepository.findByEmail("ana@tico.com")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("ana@tico.com");

        assertNotNull(result);
        assertEquals("ana@tico.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_whenEmailNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("noexiste@tico.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("noexiste@tico.com"));
    }

}
