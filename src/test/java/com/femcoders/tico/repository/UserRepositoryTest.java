package com.femcoders.tico.repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setName("Admin Test");
        adminUser.setEmail("admin@test.com");
        adminUser.setPasswordHash("hash");
        adminUser.setIsActive(true);
        adminUser.setRoles(Set.of(UserRole.ADMIN));
        userRepository.save(adminUser);

        employeeUser = new User();
        employeeUser.setName("Employee Test");
        employeeUser.setEmail("employee@test.com");
        employeeUser.setPasswordHash("hash");
        employeeUser.setIsActive(false);
        employeeUser.setRoles(Set.of(UserRole.EMPLOYEE));
        userRepository.save(employeeUser);
    }

    @Test
    void findByEmail_whenExists_returnsUser() {
        Optional<User> result = userRepository.findByEmail("admin@test.com");

        assertTrue(result.isPresent());
        assertEquals("admin@test.com", result.get().getEmail());
    }

    @Test
    void findByEmail_whenNotExists_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("noexiste@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByRolesContaining_whenAdminExists_returnsAdminList() {
        List<User> result = userRepository.findByRolesContaining(UserRole.ADMIN);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(u -> u.getRoles().contains(UserRole.ADMIN)));
    }

    @Test
    void findByRolesContainingAndIsActiveTrue_returnsOnlyActiveAdmins() {
        Page<User> result = userRepository.findByRolesContainingAndIsActiveTrue(
                UserRole.ADMIN, Pageable.unpaged());

        assertTrue(result.getContent().stream()
                .allMatch(u -> u.getRoles().contains(UserRole.ADMIN) 
                && Boolean.TRUE.equals(u.getIsActive())));
    }

    @Test
    void countByRolesContaining_returnsCorrectCount() {
        long count = userRepository.countByRolesContaining(UserRole.ADMIN);

        assertEquals(1L, count);
    }
}
