package com.backend.ms_security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.ms_security.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    @EntityGraph(attributePaths = {"user", "role"})
    List<UserRole> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "role"})
    List<UserRole> findAllByRoleId(Long roleId);

    @EntityGraph(attributePaths = {"user", "role"})
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByRoleId(Long roleId);
}
