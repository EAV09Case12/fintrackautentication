package com.example.webapi.fintrackautentication.dto.request;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Tests para RefreshTokenRequestDTO validación.
 * Cubre: token requerido y límite de tamaño.
 */
@SpringBootTest
class RefreshTokenRequestDTOTest {

    @Autowired
    private Validator validator;

    private RefreshTokenRequestDTO dto;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa un DTO válido por defecto para cada test.
         */
        dto = RefreshTokenRequestDTO.builder()
            .refreshToken("valid_refresh_token_string")
            .build();
    }

    @Test
    void acceptsValidToken() {
        /*
         * Caso de éxito: solicitud con token válido.
         * Esperado: sin violaciones de validación.
         */
        Set<ConstraintViolation<RefreshTokenRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsEmptyToken() {
        /*
         * Caso excepcional: token vacío.
         * Esperado: violación de @NotBlank.
         */
        dto.setRefreshToken("");

        Set<ConstraintViolation<RefreshTokenRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsNullToken() {
        /*
         * Caso excepcional: token es null.
         * Esperado: violación de @NotBlank.
         */
        dto.setRefreshToken(null);

        Set<ConstraintViolation<RefreshTokenRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsTokenTooLong() {
        /*
         * Caso excepcional: token excede 255 caracteres.
         * Esperado: violación de @Size.
         */
        dto.setRefreshToken("a".repeat(256));

        Set<ConstraintViolation<RefreshTokenRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void acceptsTokenAtMaxLength() {
        /*
         * Token exactamente en el límite (255 caracteres).
         * Esperado: es válido.
         */
        dto.setRefreshToken("a".repeat(255));

        Set<ConstraintViolation<RefreshTokenRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void acceptsJwtFormattedToken() {
        /*
         * Token con formato JWT (header.payload.signature).
         * Esperado: es válido si no excede 255 caracteres.
         */
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        dto.setRefreshToken(jwtToken);

        Set<ConstraintViolation<RefreshTokenRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
