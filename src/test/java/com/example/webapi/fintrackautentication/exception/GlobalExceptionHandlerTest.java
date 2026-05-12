package com.example.webapi.fintrackautentication.exception;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.example.webapi.fintrackautentication.dto.response.MessageResponseDTO;

/**
 * Tests para GlobalExceptionHandler.
 * Cubre: manejo de todas las excepciones personalizadas y estándar.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa el handler para cada test.
         */
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handlesExistingEmail() {
        /*
         * Caso: EmailAlreadyExistsException.
         * Esperado: retorna 409 CONFLICT.
         */
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("El correo ya está registrado");

        ResponseEntity<MessageResponseDTO> response = handler.handleEmailExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("ya está registrado"));
    }

    @Test
    void handlesRoleNotFound() {
        /*
         * Caso: RoleNotFoundException.
         * Esperado: retorna 500 INTERNAL_SERVER_ERROR.
         */
        RoleNotFoundException ex = new RoleNotFoundException("Rol USER no encontrado");

        ResponseEntity<MessageResponseDTO> response = handler.handleRoleNotFound(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handlesUserNotFound() {
        /*
         * Caso: UserNotFoundException.
         * Esperado: retorna 404 NOT_FOUND.
         */
        UserNotFoundException ex = new UserNotFoundException("usuario no encontrado");

        ResponseEntity<MessageResponseDTO> response = handler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("no encontrado"));
    }

    @Test
    void handlesUserNotFoundWithDefault() {
        /*
         * Caso: UserNotFoundException sin mensaje.
         * Esperado: retorna mensaje por defecto.
         */
        UserNotFoundException ex = new UserNotFoundException(null);

        ResponseEntity<MessageResponseDTO> response = handler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("usuario no encontrado", response.getBody().getMessage());
    }

    @Test
    void handlesBadCredentials() {
        /*
         * Caso: BadCredentialsException.
         * Esperado: retorna 401 UNAUTHORIZED.
         */
        BadCredentialsException ex = new BadCredentialsException("credenciales inválidas");

        ResponseEntity<MessageResponseDTO> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("inválidas"));
    }

    @Test
    void handlesBadCredentialsWithDefault() {
        /*
         * Caso: BadCredentialsException sin mensaje.
         * Esperado: retorna mensaje por defecto.
         */
        BadCredentialsException ex = new BadCredentialsException(null);

        ResponseEntity<MessageResponseDTO> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("credenciales inválidas", response.getBody().getMessage());
    }

    @Test
    void handlesInvalidRefreshToken() {
        /*
         * Caso: InvalidRefreshTokenException.
         * Esperado: retorna 403 FORBIDDEN.
         */
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("Token de refresco inválido");

        ResponseEntity<MessageResponseDTO> response = handler.handleInvalidRefreshToken(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("inválido"));
    }

    @Test
    void handlesIllegalArgument() {
        /*
         * Caso: IllegalArgumentException.
         * Esperado: retorna 400 BAD_REQUEST.
         */
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<MessageResponseDTO> response = handler.handleIllegalArg(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handlesValidationErrors() {
        /*
         * Caso: MethodArgumentNotValidException con múltiples errores.
         * Esperado: retorna 400 BAD_REQUEST con todos los mensajes.
         */
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        FieldError error1 = new FieldError("dto", "email", "Email es obligatorio");
        FieldError error2 = new FieldError("dto", "password", "Contraseña debe tener 8 caracteres");
        
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<MessageResponseDTO> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Email es obligatorio"));
        assertTrue(response.getBody().getMessage().contains("Contraseña debe tener 8 caracteres"));
    }

    @Test
    void handlesValidationWithDefault() {
        /*
         * Caso: MethodArgumentNotValidException sin errores de campo.
         * Esperado: retorna mensaje por defecto.
         */
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<MessageResponseDTO> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validación fallida", response.getBody().getMessage());
    }

    @Test
    void handlesGenericException() {
        /*
         * Caso: Exception genérica.
         * Esperado: retorna 500 INTERNAL_SERVER_ERROR.
         */
        RuntimeException ex = new RuntimeException("Error inesperado");

        ResponseEntity<MessageResponseDTO> response = handler.handleAll(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("inesperado"));
    }

    @Test
    void handlesGenericExceptionWithDefault() {
        /*
         * Caso: Exception genérica sin mensaje.
         * Esperado: retorna mensaje por defecto.
         */
        RuntimeException ex = new RuntimeException((String) null);

        ResponseEntity<MessageResponseDTO> response = handler.handleAll(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().getMessage());
    }
}
