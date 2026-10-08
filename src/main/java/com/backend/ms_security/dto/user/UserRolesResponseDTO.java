package com.backend.ms_security.dto.user;

import java.util.List;

import com.backend.ms_security.dto.userrole.UserRoleResponseDTO;

import lombok.Value;

@Value
public class UserRolesResponseDTO {
    Long id;
    String name;
    String email;
    List<UserRoleResponseDTO> roles;
}
