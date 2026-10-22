package com.backend.ms_security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.ms_security.dto.user.CreateUserDTO;
import com.backend.ms_security.dto.user.UserResponseDTO;
import com.backend.ms_security.mapper.ProfileMapper;
import com.backend.ms_security.mapper.RoleMapper;
import com.backend.ms_security.mapper.SessionMapper;
import com.backend.ms_security.mapper.UserMapper;
import com.backend.ms_security.mapper.UserRoleMapper;
import com.backend.ms_security.repository.UserRepository;
import com.backend.ms_security.service.UserService;

class UserPasswordEncodingTest {

    @Test
    void createShouldHashPasswordBeforePersistingUser() {
        UserRepository userRepository = mock(UserRepository.class);
        UserMapper userMapper = new UserMapper(
                new ProfileMapper(),
                new SessionMapper(),
                new UserRoleMapper(new RoleMapper())
        );
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        UserService userService = new UserService(userRepository, userMapper, passwordEncoder);

        CreateUserDTO dto = new CreateUserDTO();
        dto.setName("Ana Gómez");
        dto.setEmail("ana@example.com");
        dto.setPassword("ClaveSegura123!");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> {
            com.backend.ms_security.entity.User user = invocation.getArgument(0);
            assertNotEquals(dto.getPassword(), user.getPassword());
            assertTrue(passwordEncoder.matches(dto.getPassword(), user.getPassword()));
            return user;
        });

        UserResponseDTO response = userService.create(dto);

        assertEquals("Ana Gómez", response.getName());
        assertEquals("ana@example.com", response.getEmail());
        verify(userRepository).save(any());
    }
}
