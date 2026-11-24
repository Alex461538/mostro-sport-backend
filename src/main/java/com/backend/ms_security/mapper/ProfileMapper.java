package com.backend.ms_security.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.ms_security.dto.profile.CreateProfileDTO;
import com.backend.ms_security.dto.profile.ProfileResponseDTO;
import com.backend.ms_security.dto.profile.UpdateProfileDTO;
import com.backend.ms_security.entity.Profile;

@Component
public class ProfileMapper {
    public Profile toEntity(CreateProfileDTO dto) {
        Profile profile = new Profile();
        profile.setPhone(dto.getPhone());
        profile.setBirthDate(dto.getBirthDate());
        profile.setPhotoUrl(dto.getPhotoUrl());
        return profile;
    }

    public void updateEntity(CreateProfileDTO dto, Profile profile) {
        profile.setPhone(dto.getPhone());
        profile.setBirthDate(dto.getBirthDate());
        profile.setPhotoUrl(dto.getPhotoUrl());
    }

    public void updateEntity(UpdateProfileDTO dto, Profile profile) {
        profile.setPhone(dto.getPhone());
        profile.setBirthDate(dto.getBirthDate());
        profile.setPhotoUrl(dto.getPhotoUrl());
    }

    public ProfileResponseDTO toResponseDTO(Profile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileResponseDTO(
                profile.getId(),
                profile.getPhone(),
                profile.getBirthDate(),
                profile.getPhotoUrl()
        );
    }

    public List<ProfileResponseDTO> toResponseDTOList(List<Profile> profiles) {
        return profiles.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
