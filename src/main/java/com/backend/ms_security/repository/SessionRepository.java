package com.backend.ms_security.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.ms_security.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findAllByUserId(Long userId);

    Optional<Session> findByIdAndUserId(Long sessionId, Long userId);

    Optional<Session> findByToken(String token);

    Optional<Session> findByUserIdAndToken(Long userId, String token);

    Optional<Session> findTopByUserIdAndCode2FAIsNotNullOrderByExpirationDesc(Long userId);

    // Solo desafios 2FA reales: excluye las sesiones JWT (code2FA nulo o vacio).
    Optional<Session> findTopByUserIdAndCode2FANotOrderByExpirationDesc(Long userId, String code2FA);

    @Transactional
    void deleteByExpirationBefore(ZonedDateTime moment);

    boolean existsByToken(String token);

    boolean existsByTokenAndIdNot(String token, Long id);
}
