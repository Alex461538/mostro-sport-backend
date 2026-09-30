package com.backend.ms_security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.ms_security.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
