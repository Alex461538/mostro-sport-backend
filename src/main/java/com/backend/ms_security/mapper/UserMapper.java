package com.backend.ms_security.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.ms_security.dto.user.CreateUserDTO;
import com.backend.ms_security.dto.user.UpdateUserDTO;
import com.backend.ms_security.dto.user.UserResponseDTO;
import com.backend.ms_security.entity.User;

@Component
public class UserMapper {
    public User toEntity(CreateUserDTO dto) {
        User user = new User();

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        return user;
    }

    public void updateEntity(UpdateUserDTO dto, User user)
    {
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }
    }

    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public List<UserResponseDTO> toResponseDTOList(List<User> users)
    {
        return users.stream()
            .map(this::toResponseDTO)
            .toList();
    }
}
