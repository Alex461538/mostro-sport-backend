package com.backend.ms_security.service;

import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.user.CreateUserDTO;
import com.backend.ms_security.dto.user.UpdateUserDTO;
import com.backend.ms_security.dto.user.UserDetailResponseDTO;
import com.backend.ms_security.dto.user.UserResponseDTO;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.UserMapper;
import com.backend.ms_security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDTO create(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "There is already a user with this email."
            );
        }
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public List<UserResponseDTO> findAll() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseDTOList(users);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    public UserDetailResponseDTO findById(Long id) {
        User user = userRepository.findWithProfileById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "User not found with id: " + id
                ));
        return userMapper.toDetailResponseDTO(user);
    }

    public UserDetailResponseDTO findByIdWithProfile(Long id) {
        return findById(id);
    }

    public UserResponseDTO update(Long id, UpdateUserDTO dto) {
        User user = findUser(id);
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "This email is already used by another user."
            );
        }
        userMapper.updateEntity(dto, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }

    public void delete(Long id) {
        User user = findUser(id);
        userRepository.delete(user);
    }
}
