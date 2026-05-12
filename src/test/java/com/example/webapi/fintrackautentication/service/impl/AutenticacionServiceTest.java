package com.example.webapi.fintrackautentication.service.impl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.example.webapi.fintrackautentication.config.SecurityProperties;
import com.example.webapi.fintrackautentication.domain.RefreshToken;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.dto.request.LoginRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;
import com.example.webapi.fintrackautentication.exception.InvalidRefreshTokenException;
import com.example.webapi.fintrackautentication.exception.UserNotFoundException;
import com.example.webapi.fintrackautentication.helper.TestDataBuilder;
import com.example.webapi.fintrackautentication.helper.TokenTestHelper;
import com.example.webapi.fintrackautentication.repository.AuditoriaRepository;
import com.example.webapi.fintrackautentication.repository.RefreshTokenRepository;
import com.example.webapi.fintrackautentication.repository.UserRepository;
import com.example.webapi.fintrackautentication.service.TokenService;

/**
 * Tests para AutenticacionServiceImpl.
 * Cubre: login, refresh token, logout con casos de éxito y excepcionales.
 */
@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private SecurityProperties securityProperties;

    @InjectMocks
    private AutenticacionServiceImpl autenticacionService;

    private LoginRequestDTO validLogin;
    private User validUser;
    private RefreshTokenRequestDTO refreshRequest;
    private AuthenticationResponseDTO authResponse;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa datos comunes para todos los tests.
         */
        validUser = TestDataBuilder.createValidUser();
        validLogin = TestDataBuilder.createValidLoginRequest();
        refreshRequest = TestDataBuilder.createValidRefreshTokenRequest();
        authResponse = new AuthenticationResponseDTO(
            TokenTestHelper.generateValidAccessToken(validUser.getEmail()),
            TokenTestHelper.generateValidRefreshToken(validUser.getEmail()),
            "Bearer",
            900000L,
            validUser.getId(),
            List.of("USER")
        );
    }

    // === AUTHENTICATE TESTS ===

    @Test
    void authenticatesUserSuccessfully() {
        /*
         * Caso de éxito: login con credenciales válidas.
         * Esperado: retorna AuthenticationResponseDTO con tokens.
         */
        when(userRepository.findByEmail(validLogin.getEmail()))
            .thenReturn(Optional.of(validUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(validLogin.getEmail(), validLogin.getPassword()));
        when(tokenService.createTokensForUser(validUser))
            .thenReturn(authResponse);

        AuthenticationResponseDTO result = autenticacionService.authenticate(validLogin);

        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertEquals(validUser.getId(), result.getUserId());
        verify(auditoriaRepository).save(any());
    }

    @Test
    void throwsWhenUserNotFound() {
        /*
         * Caso excepcional: usuario no existe en BD.
         * Esperado: lanza UserNotFoundException.
         */
        when(userRepository.findByEmail(validLogin.getEmail()))
            .thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> {
            autenticacionService.authenticate(validLogin);
        });
        assertNotNull(ex);

        verify(tokenService, never()).createTokensForUser(any());
        verify(auditoriaRepository).save(any());
    }

    @Test
    void rejectsBlockedAccount() {
        /*
         * Caso excepcional: cuenta está bloqueada.
         * Esperado: lanza BadCredentialsException con mensaje "Cuenta bloqueada".
         */
        User blockedUser = TestDataBuilder.createBlockedUser();
        when(userRepository.findByEmail(validLogin.getEmail()))
            .thenReturn(Optional.of(blockedUser));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> {
            autenticacionService.authenticate(validLogin);
        });
        assertNotNull(ex);

        verify(tokenService, never()).createTokensForUser(any());
    }

    @Test
    void rejectsInactiveAccount() {
        /*
         * Caso excepcional: cuenta está inactiva (estado=false).
         * Esperado: lanza BadCredentialsException.
         */
        User inactiveUser = TestDataBuilder.createInactiveUser();
        when(userRepository.findByEmail(validLogin.getEmail()))
            .thenReturn(Optional.of(inactiveUser));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> {
            autenticacionService.authenticate(validLogin);
        });
        assertNotNull(ex);

        verify(tokenService, never()).createTokensForUser(any());
    }

    @Test
    void incrementsFailedAttemptsOnBadCredentials() {
        /*
         * Caso excepcional: credenciales inválidas (password incorrecto).
         * Esperado: incrementa intentosFallidos, lanza BadCredentialsException.
         */
        when(userRepository.findByEmail(validLogin.getEmail()))
            .thenReturn(Optional.of(validUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("credenciales inválidas"));
        when(securityProperties.getMaxIntentosFallidos())
            .thenReturn(5);
        when(userRepository.save(any(User.class)))
            .thenReturn(validUser);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> {
            autenticacionService.authenticate(validLogin);
        });
        assertNotNull(ex);

        assertEquals(1, validUser.getIntentosFallidos());
        verify(userRepository).save(validUser);
    }

    @Test
    void locksAccountAfterMaxAttempts() {
        /*
         * Caso excepcional: cuenta se bloquea tras superar máximo de intentos.
         * Esperado: cuentaBloqueada se establece en true.
         */
        User userWithAttempts = TestDataBuilder.createUserWithFailedAttempts(4);
        when(userRepository.findByEmail(validLogin.getEmail()))
            .thenReturn(Optional.of(userWithAttempts));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("credenciales inválidas"));
        when(securityProperties.getMaxIntentosFallidos())
            .thenReturn(5);
        when(userRepository.save(any(User.class)))
            .thenReturn(userWithAttempts);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> {
            autenticacionService.authenticate(validLogin);
        });
        assertNotNull(ex);

        assertEquals(5, userWithAttempts.getIntentosFallidos());
        assertTrue(userWithAttempts.isCuentaBloqueada());
    }

    @Test
    void resetsFailedAttemptsOnSuccess() {
        /*
         * Verifica que intentosFallidos se resetean al login exitoso.
         * Esperado: intentosFallidos=0.
         */
        User userWithAttempts = TestDataBuilder.createUserWithFailedAttempts(2);
        when(userRepository.findByEmail(validLogin.getEmail()))
            .thenReturn(Optional.of(userWithAttempts));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(validLogin.getEmail(), validLogin.getPassword()));
        when(tokenService.createTokensForUser(userWithAttempts))
            .thenReturn(authResponse);
        when(userRepository.save(any(User.class)))
            .thenReturn(userWithAttempts);

        autenticacionService.authenticate(validLogin);

        assertEquals(0, userWithAttempts.getIntentosFallidos());
    }

    // === REFRESH TESTS ===

    @Test
    void refreshesTokenSuccessfully() {
        /*
         * Caso de éxito: refresh token válido genera nuevos tokens.
         * Esperado: retorna AuthenticationResponseDTO con nuevos tokens.
         */
        when(tokenService.refresh(refreshRequest.getRefreshToken()))
            .thenReturn(Optional.of(authResponse));
        when(refreshTokenRepository.findByToken(refreshRequest.getRefreshToken()))
            .thenReturn(Optional.of(TestDataBuilder.createValidRefreshToken(validUser)));

        AuthenticationResponseDTO result = autenticacionService.refresh(refreshRequest);

        assertNotNull(result);
        verify(auditoriaRepository).save(any());
    }

    @Test
    void throwsOnInvalidRefreshToken() {
        /*
         * Caso excepcional: token de refresh no es válido o expiró.
         * Esperado: lanza InvalidRefreshTokenException.
         */
        when(tokenService.refresh(refreshRequest.getRefreshToken()))
            .thenReturn(Optional.empty());

        InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class, () -> {
            autenticacionService.refresh(refreshRequest);
        });
        assertNotNull(ex);

        verify(auditoriaRepository).save(any());
    }

    // === LOGOUT TESTS ===

    @Test
    void logsOutSuccessfully() {
        /*
         * Caso de éxito: revoca el refresh token.
         * Esperado: no lanza excepciones, token se marca revocado.
         */
        RefreshToken rt = TestDataBuilder.createValidRefreshToken(validUser);
        when(refreshTokenRepository.findByToken(refreshRequest.getRefreshToken()))
            .thenReturn(Optional.of(rt));
        doNothing().when(tokenService).revokeByToken(refreshRequest.getRefreshToken());

        assertDoesNotThrow(() -> {
            autenticacionService.logout(refreshRequest);
        });

        verify(tokenService).revokeByToken(refreshRequest.getRefreshToken());
        verify(auditoriaRepository).save(any());
    }

    @Test
    void logsOutWithInvalidToken() {
        /*
         * Caso excepcional: logout con token que no existe.
         * Esperado: se registra auditoría pero no lanza excepción (TokenService lo maneja).
         */
        when(refreshTokenRepository.findByToken(refreshRequest.getRefreshToken()))
            .thenReturn(Optional.empty());
        doNothing().when(tokenService).revokeByToken(refreshRequest.getRefreshToken());

        assertDoesNotThrow(() -> {
            autenticacionService.logout(refreshRequest);
        });

        verify(auditoriaRepository).save(any());
    }
}
