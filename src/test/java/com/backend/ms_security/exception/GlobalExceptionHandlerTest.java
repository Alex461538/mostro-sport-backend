package com.backend.ms_security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApplicationExceptionReturnsMappedHttpStatus() {
        ResponseEntity<Map<String, String>> response = handler.handleApplicationException(
                new ApplicationException(ErrorCase.NOT_FOUND, "User not found")
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().get("errorCase"));
        assertEquals("User not found", response.getBody().get("message"));
    }
}
