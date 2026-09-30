package com.backend.ms_security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.ms_security.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
