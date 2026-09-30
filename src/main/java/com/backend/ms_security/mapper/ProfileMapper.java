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
        return profile;
    }

    public void updateEntity(UpdateProfileDTO dto, Profile profile) {
        profile.setPhone(dto.getPhone());
        profile.setBirthDate(dto.getBirthDate());
    }

    public ProfileResponseDTO toResponseDTO(Profile profile) {
        return new ProfileResponseDTO(
                profile.getId(),
                profile.getPhone(),
                profile.getBirthDate()
        );
    }

    public List<ProfileResponseDTO> toResponseDTOList(List<Profile> profiles) {
        return profiles.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
