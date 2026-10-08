package com.backend.ms_security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.ms_security.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findAllByUserId(Long userId);

    Optional<Session> findByIdAndUserId(Long sessionId, Long userId);

    boolean existsByToken(String token);

    boolean existsByTokenAndIdNot(String token, Long id);
}
