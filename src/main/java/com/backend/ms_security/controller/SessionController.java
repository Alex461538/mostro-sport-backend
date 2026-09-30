package com.backend.ms_security.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ms_security.dto.session.CreateSessionDTO;
import com.backend.ms_security.dto.session.SessionResponseDTO;
import com.backend.ms_security.dto.session.UpdateSessionDTO;
import com.backend.ms_security.service.SessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sessions/")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponseDTO create(@Valid @RequestBody CreateSessionDTO dto) {
        return sessionService.create(dto);
    }

    @GetMapping()
    public List<SessionResponseDTO> findAll() {
        return sessionService.findAll();
    }

    @GetMapping("/{id}")
    public SessionResponseDTO findById(@PathVariable Long id) {
        return sessionService.findById(id);
    }

    @PutMapping("/{id}")
    public SessionResponseDTO update(@PathVariable Long id, @Valid @RequestBody UpdateSessionDTO dto) {
        return sessionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sessionService.delete(id);
    }
}
