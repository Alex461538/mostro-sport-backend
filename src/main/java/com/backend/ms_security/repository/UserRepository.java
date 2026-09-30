package com.backend.ms_security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.backend.ms_security.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
