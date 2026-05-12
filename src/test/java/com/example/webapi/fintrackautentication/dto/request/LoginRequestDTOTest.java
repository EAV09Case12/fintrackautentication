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
 * Tests para LoginRequestDTO validación.
 * Cubre: email y contraseña requeridos y con límites de tamaño.
 */
@SpringBootTest
class LoginRequestDTOTest {

    @Autowired
    private Validator validator;

    private LoginRequestDTO dto;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa un DTO válido por defecto para cada test.
         */
        dto = LoginRequestDTO.builder()
            .email("test@example.com")
            .password("Password123")
            .build();
    }

    @Test
    void acceptsValidCredentials() {
        /*
         * Caso de éxito: solicitud de login con datos válidos.
         * Esperado: sin violaciones de validación.
         */
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsEmptyEmail() {
        /*
         * Caso excepcional: email vacío.
         * Esperado: violación de @NotBlank.
         */
        dto.setEmail("");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("obligatorio")));
    }

    @Test
    void rejectsNullEmail() {
        /*
         * Caso excepcional: email es null.
         * Esperado: violación de @NotBlank.
         */
        dto.setEmail(null);

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsEmailTooLong() {
        /*
         * Caso excepcional: email excede 100 caracteres.
         * Esperado: violación de @Size.
         */
        dto.setEmail("a".repeat(101) + "@example.com");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsEmptyPassword() {
        /*
         * Caso excepcional: contraseña vacía.
         * Esperado: violación de @NotBlank.
         */
        dto.setPassword("");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("obligatoria")));
    }

    @Test
    void rejectsNullPassword() {
        /*
         * Caso excepcional: contraseña es null.
         * Esperado: violación de @NotBlank.
         */
        dto.setPassword(null);

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsPasswordTooShort() {
        /*
         * Caso excepcional: contraseña menor a 8 caracteres.
         * Esperado: violación de @Size.
         */
        dto.setPassword("Pass1");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsPasswordTooLong() {
        /*
         * Caso excepcional: contraseña mayor a 20 caracteres.
         * Esperado: violación de @Size.
         */
        dto.setPassword("Password1234567890123");  // 21 caracteres

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void acceptsPasswordBoundaries() {
        /*
         * Verifica límites válidos de contraseña (8-20 caracteres).
         * Esperado: ambos límites pasan validación.
         */
        // Exactamente 8 caracteres
        dto.setPassword("Pass1234");
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());

        // Exactamente 20 caracteres
        dto.setPassword("Pass123456789012345");
        violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
