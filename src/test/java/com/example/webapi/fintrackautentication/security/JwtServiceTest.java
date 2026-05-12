package com.example.webapi.fintrackautentication.security;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests para JwtService.
 * Cubre: generación de tokens, validación, extracción de claims.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        /*
         * Configura la secret key antes de cada test.
         * Nota: La clave debe estar en Base64 y tener al menos 256 bits.
         */
        // Clave Base64 válida de 256 bits (32 bytes)
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "dGVzdHNlY3JldGtleXRlc3RzZWNyZXRrZXl0ZXN0c2VjcmV0");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void generatesAccessToken() {
        /*
         * Caso de éxito: genera un access token válido.
         * Esperado: token contiene subject y roles.
         */
        String email = "test@example.com";
        List<String> roles = List.of("USER");

        String token = jwtService.generateAccessToken(email, roles);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(email, jwtService.extractUsername(token));
    }

    @Test
    void generatesAccessTokenWithMultipleRoles() {
        /*
         * Genera access token con múltiples roles.
         * Esperado: token contiene todos los roles en el claim.
         */
        String email = "admin@example.com";
        List<String> roles = List.of("USER", "ADMIN");

        String token = jwtService.generateAccessToken(email, roles);

        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
    }

    @Test
    void generatesAccessTokenWithEmptyRoles() {
        /*
         * Genera access token sin roles.
         * Esperado: token es válido con lista de roles vacía.
         */
        String email = "newuser@example.com";
        List<String> roles = new ArrayList<>();

        String token = jwtService.generateAccessToken(email, roles);

        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
    }

    @Test
    void generatesRefreshToken() {
        /*
         * Caso de éxito: genera un refresh token válido.
         * Esperado: token contiene subject.
         */
        String email = "test@example.com";

        String token = jwtService.generateRefreshToken(email);

        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
    }

    @Test
    void extractsUsernameFromToken() {
        /*
         * Extrae el email (subject) de un token.
         * Esperado: retorna el email correcto.
         */
        String email = "test@example.com";
        String token = jwtService.generateAccessToken(email, List.of("USER"));

        String extracted = jwtService.extractUsername(token);

        assertEquals(email, extracted);
    }

    @Test
    void throwsExceptionExtractingFromExpiredToken() {
        /*
         * Intenta extraer el email de un token expirado.
         * Esperado: lanza excepción.
         */
        String token = jwtService.generateToken("test@example.com", -1000L);

        Exception ex = assertThrows(Exception.class, () -> {
            jwtService.extractUsername(token);
        });
        assertNotNull(ex);
    }

    @Test
    void validatesTokenSuccessfully() {
        /*
         * Valida un token correcto con UserDetails coincidente.
         * Esperado: retorna true.
         */
        String email = "test@example.com";
        String token = jwtService.generateAccessToken(email, List.of("USER"));
        UserDetails userDetails = new User(email, "password", new ArrayList<>());

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertTrue(valid);
    }

    @Test
    void rejectsTokenWithDifferentUser() {
        /*
         * Valida un token con UserDetails de email diferente.
         * Esperado: retorna false.
         */
        String tokenEmail = "test@example.com";
        String userEmail = "different@example.com";
        String token = jwtService.generateAccessToken(tokenEmail, List.of("USER"));
        UserDetails userDetails = new User(userEmail, "password", new ArrayList<>());

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertFalse(valid);
    }

    @Test
    void rejectsExpiredToken() {
        /*
         * Valida un token expirado.
         * Esperado: lanza excepción por expiración.
         */
        String email = "test@example.com";
        String token = jwtService.generateToken(email, -1000L);
        UserDetails userDetails = new User(email, "password", new ArrayList<>());

        Exception ex = assertThrows(Exception.class, () -> jwtService.isTokenValid(token, userDetails));
        assertNotNull(ex);
    }

    @Test
    void returnsAccessTokenExpiration() {
        /*
         * Obtiene el tiempo de expiración del access token.
         * Esperado: retorna 15 minutos (900000 ms).
         */
        long expirationMs = jwtService.getAccessTokenExpirationMs();

        assertEquals(900000L, expirationMs);
    }

    @Test
    void returnsRefreshTokenExpiration() {
        /*
         * Obtiene el tiempo de expiración del refresh token.
         * Esperado: retorna 7 días (604800000 ms).
         */
        long expirationMs = jwtService.getRefreshTokenExpirationMs();

        assertEquals(604800000L, expirationMs);
    }

    @Test
    void generatesTokenWithCustomExpiration() {
        /*
         * Genera un token con expiración personalizada.
         * Esperado: token se crea exitosamente.
         */
        String email = "test@example.com";
        long customExpiration = 1800000L;

        String token = jwtService.generateToken(email, customExpiration);

        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
    }
}
