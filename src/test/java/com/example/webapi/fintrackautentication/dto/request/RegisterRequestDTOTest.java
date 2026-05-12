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
 * Tests para RegisterRequestDTO validación.
 * Cubre: email válido/inválido, password con complejidad.
 */
@SpringBootTest
class RegisterRequestDTOTest {

    @Autowired
    private Validator validator;

    private RegisterRequestDTO dto;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa un DTO válido por defecto para cada test.
         */
        dto = RegisterRequestDTO.builder()
            .email("valid@example.com")
            .password("ValidPassword123")
            .build();
    }

    @Test
    void acceptsValidRequest() {
        /*
         * Caso de éxito: solicitud con datos válidos.
         * Esperado: sin violaciones de validación.
         */
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsInvalidEmailFormat() {
        /*
         * Caso excepcional: email con formato inválido.
         * Esperado: violación de @Email.
         */
        dto.setEmail("invalid-email");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("formato válido")));
    }

    @Test
    void rejectsEmptyEmail() {
        /*
         * Caso excepcional: email vacío.
         * Esperado: violación de @NotBlank.
         */
        dto.setEmail("");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

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

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsEmailTooLong() {
        /*
         * Caso excepcional: email excede 100 caracteres.
         * Esperado: violación de @Size.
         */
        dto.setEmail("a".repeat(101) + "@example.com");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("máximo")));
    }

    @Test
    void rejectsPasswordMissingUppercase() {
        /*
         * Caso excepcional: contraseña sin mayúscula.
         * Esperado: violación de @Pattern.
         */
        dto.setPassword("invalidpassword123");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("mayúscula")));
    }

    @Test
    void rejectsPasswordMissingLowercase() {
        /*
         * Caso excepcional: contraseña sin minúscula.
         * Esperado: violación de @Pattern.
         */
        dto.setPassword("INVALIDPASSWORD123");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsPasswordMissingDigit() {
        /*
         * Caso excepcional: contraseña sin número.
         * Esperado: violación de @Pattern.
         */
        dto.setPassword("InvalidPassword");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("número")));
    }

    @Test
    void rejectsPasswordTooShort() {
        /*
         * Caso excepcional: contraseña menor a 8 caracteres.
         * Esperado: violación de @Size.
         */
        dto.setPassword("Pwd1");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("entre")));
    }

    @Test
    void rejectsPasswordTooLong() {
        /*
         * Caso excepcional: contraseña mayor a 100 caracteres.
         * Esperado: violación de @Size.
         */
        dto.setPassword("A1" + "a".repeat(100));

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsEmptyPassword() {
        /*
         * Caso excepcional: contraseña vacía.
         * Esperado: violación de @NotBlank.
         */
        dto.setPassword("");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsNullPassword() {
        /*
         * Caso excepcional: contraseña es null.
         * Esperado: violación de @NotBlank.
         */
        dto.setPassword(null);

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void acceptsValidPasswordVariations() {
        /*
         * Verifica múltiples contraseñas válidas.
         * Esperado: todas pasan validación.
         */
        String[] validPasswords = {
            "Password1",
            "MySecurePass2023",
            "Test@Password1",
            "Complex!Pass99"
        };

        for (String password : validPasswords) {
            dto.setPassword(password);
            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Password should be valid: " + password);
        }
    }
}
