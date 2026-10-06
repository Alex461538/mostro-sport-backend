package com.backend.ms_security.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.profile.CreateProfileDTO;
import com.backend.ms_security.dto.profile.ProfileResponseDTO;
import com.backend.ms_security.dto.profile.UpdateProfileDTO;
import com.backend.ms_security.entity.Profile;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.ProfileMapper;
import com.backend.ms_security.repository.ProfileRepository;
import com.backend.ms_security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    public ProfileResponseDTO create(CreateProfileDTO dto) {
        Profile profile = profileMapper.toEntity(dto);
        Profile savedProfile = profileRepository.save(profile);
        return profileMapper.toResponseDTO(savedProfile);
    }

    public ProfileResponseDTO create(Long userId, CreateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        if (profileRepository.existsByUserId(userId)) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "The user already has a profile."
            );
        }

        Profile profile = profileMapper.toEntity(dto);
        profile.setUser(user);
        user.setProfile(profile);

        Profile savedProfile = profileRepository.save(profile);
        return profileMapper.toResponseDTO(savedProfile);
    }

    public List<ProfileResponseDTO> findAll() {
        return profileMapper.toResponseDTOList(profileRepository.findAll());
    }

    private Profile findProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Profile not found with id: " + id
                ));
    }

    private Profile findProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Profile not found for user with id: " + userId
                ));
    }

    public ProfileResponseDTO findById(Long id) {
        return profileMapper.toResponseDTO(findProfile(id));
    }

    public ProfileResponseDTO findByUserId(Long userId) {
        return profileMapper.toResponseDTO(findProfileByUserId(userId));
    }

    public ProfileResponseDTO update(Long id, UpdateProfileDTO dto) {
        Profile profile = findProfile(id);
        profileMapper.updateEntity(dto, profile);
        Profile updatedProfile = profileRepository.save(profile);
        return profileMapper.toResponseDTO(updatedProfile);
    }

    public ProfileResponseDTO update(Long userId, CreateProfileDTO dto) {
        Profile profile = findProfileByUserId(userId);
        profileMapper.updateEntity(dto, profile);
        Profile updatedProfile = profileRepository.save(profile);
        return profileMapper.toResponseDTO(updatedProfile);
    }

    public void delete(Long id) {
        profileRepository.delete(findProfile(id));
    }

    public void deleteByUserId(Long userId) {
        profileRepository.delete(findProfileByUserId(userId));
    }
}
