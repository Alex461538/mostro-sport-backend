package com.backend.ms_security.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.session.CreateSessionDTO;
import com.backend.ms_security.dto.session.SessionResponseDTO;
import com.backend.ms_security.dto.session.UpdateSessionDTO;
import com.backend.ms_security.entity.Session;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.SessionMapper;
import com.backend.ms_security.repository.SessionRepository;
import com.backend.ms_security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SessionMapper sessionMapper;

    public SessionResponseDTO create(CreateSessionDTO dto) {
        throw new ApplicationException(
                ErrorCase.INVALID_OPERATION,
                "A session must be created for a specific user."
        );
    }

    public SessionResponseDTO create(Long userId, CreateSessionDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        if (sessionRepository.existsByToken(dto.getToken())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "There is already a session with this token."
            );
        }

        Session session = sessionMapper.toEntity(dto);
        session.setUser(user);
        user.getSessions().add(session);

        Session savedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(savedSession);
    }

    public List<SessionResponseDTO> findAll() {
        return sessionMapper.toResponseDTOList(sessionRepository.findAll());
    }

    public List<SessionResponseDTO> findAllByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApplicationException(
                    ErrorCase.NOT_FOUND,
                    "User not found with id: " + userId
            );
        }

        return sessionMapper.toResponseDTOList(sessionRepository.findAllByUserId(userId));
    }

    private Session findSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Session not found with id: " + id
                ));
    }

    private Session findSession(Long userId, Long sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Session not found with id: " + sessionId + " for user " + userId
                ));
    }

    public SessionResponseDTO findById(Long id) {
        return sessionMapper.toResponseDTO(findSession(id));
    }

    public SessionResponseDTO findById(Long userId, Long sessionId) {
        return sessionMapper.toResponseDTO(findSession(userId, sessionId));
    }

    public SessionResponseDTO update(Long id, UpdateSessionDTO dto) {
        Session session = findSession(id);
        sessionMapper.updateEntity(dto, session);
        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(updatedSession);
    }

    public SessionResponseDTO update(Long userId, Long sessionId, UpdateSessionDTO dto) {
        Session session = findSession(userId, sessionId);

        if (sessionRepository.existsByTokenAndIdNot(dto.getToken(), sessionId)) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "There is already another session with this token."
            );
        }

        sessionMapper.updateEntity(dto, session);
        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(updatedSession);
    }

    public void delete(Long id) {
        sessionRepository.delete(findSession(id));
    }

    public void delete(Long userId, Long sessionId) {
        sessionRepository.delete(findSession(userId, sessionId));
    }
}
