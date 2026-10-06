package com.backend.ms_security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.backend.ms_security.dto.permission.CreatePermissionDTO;
import com.backend.ms_security.dto.profile.CreateProfileDTO;
import com.backend.ms_security.dto.role.CreateRoleDTO;
import com.backend.ms_security.dto.session.CreateSessionDTO;
import com.backend.ms_security.entity.Permission;
import com.backend.ms_security.entity.Profile;
import com.backend.ms_security.entity.Role;
import com.backend.ms_security.entity.Session;
import com.backend.ms_security.entity.User;

class CrudEntitiesTest {

    @Test
    void entitiesAndDtosShouldBeInstantiable() {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPassword("secret123");

        Profile profile = new Profile();
        profile.setPhone("123456789");
        profile.setBirthDate(ZonedDateTime.now());
        profile.setUser(user);
        user.setProfile(profile);
        assertNotNull(profile);
        assertNotNull(profile.getUser());
        assertNotNull(user.getProfile());

        Session session = new Session();
        session.setToken("token-123");
        session.setExpiration(ZonedDateTime.now());
        session.setCode2FA("456789");
        assertNotNull(session);

        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("Administrator");
        assertNotNull(role);

        Permission permission = new Permission();
        permission.setUrl("/api/users");
        permission.setMethod("GET");
        permission.setModel("User");
        assertNotNull(permission);

        CreateProfileDTO profileDto = new CreateProfileDTO();
        profileDto.setPhone("987654321");
        profileDto.setBirthDate(ZonedDateTime.now());
        assertNotNull(profileDto);

        CreateSessionDTO sessionDto = new CreateSessionDTO();
        sessionDto.setToken("abc");
        sessionDto.setExpiration(ZonedDateTime.now());
        sessionDto.setCode2FA("123456");
        assertNotNull(sessionDto);

        CreateRoleDTO roleDto = new CreateRoleDTO();
        roleDto.setName("USER");
        roleDto.setDescription("Standard user");
        assertNotNull(roleDto);

        CreatePermissionDTO permissionDto = new CreatePermissionDTO();
        permissionDto.setUrl("/api/roles");
        permissionDto.setMethod("POST");
        permissionDto.setModel("Role");
        assertNotNull(permissionDto);
    }
}
