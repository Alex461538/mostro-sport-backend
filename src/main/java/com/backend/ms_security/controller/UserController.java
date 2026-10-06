package com.backend.ms_security.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ms_security.dto.profile.CreateProfileDTO;
import com.backend.ms_security.dto.user.CreateUserDTO;
import com.backend.ms_security.dto.user.UpdateUserDTO;
import com.backend.ms_security.dto.user.UserDetailResponseDTO;
import com.backend.ms_security.dto.user.UserResponseDTO;
import com.backend.ms_security.service.ProfileService;
import com.backend.ms_security.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProfileService profileService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody CreateUserDTO dto) {
        return userService.create(dto);
    }

    @GetMapping()
    public List<UserResponseDTO> findAll() {
        return userService.findAll();
    }
    
    @GetMapping("/{id}")
    public UserDetailResponseDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    @PutMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @PostMapping("/{userId}/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public com.backend.ms_security.dto.profile.ProfileResponseDTO createProfile(
            @PathVariable Long userId,
            @Valid @RequestBody CreateProfileDTO dto) {
        return profileService.create(userId, dto);
    }

    @PutMapping("/{userId}/profile")
    public com.backend.ms_security.dto.profile.ProfileResponseDTO updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody CreateProfileDTO dto) {
        return profileService.update(userId, dto);
    }

    @GetMapping("/{userId}/profile")
    public com.backend.ms_security.dto.profile.ProfileResponseDTO findProfile(@PathVariable Long userId) {
        return profileService.findByUserId(userId);
    }

    @DeleteMapping("/{userId}/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable Long userId) {
        profileService.deleteByUserId(userId);
    }
}
