package com.backend.ms_security.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ms_security.dto.session.CreateSessionDTO;
import com.backend.ms_security.dto.session.SessionResponseDTO;
import com.backend.ms_security.dto.session.UpdateSessionDTO;
import com.backend.ms_security.entity.Session;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.mapper.SessionMapper;
import com.backend.ms_security.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    public SessionResponseDTO create(CreateSessionDTO dto) {
        Session session = sessionMapper.toEntity(dto);
        Session savedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(savedSession);
    }

    public List<SessionResponseDTO> findAll() {
        return sessionMapper.toResponseDTOList(sessionRepository.findAll());
    }

    private Session findSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Session not found with id: " + id
                ));
    }

    public SessionResponseDTO findById(Long id) {
        return sessionMapper.toResponseDTO(findSession(id));
    }

    public SessionResponseDTO update(Long id, UpdateSessionDTO dto) {
        Session session = findSession(id);
        sessionMapper.updateEntity(dto, session);
        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toResponseDTO(updatedSession);
    }

    public void delete(Long id) {
        sessionRepository.delete(findSession(id));
    }
}
