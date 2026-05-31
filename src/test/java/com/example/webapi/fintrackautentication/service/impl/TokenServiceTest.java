package com.example.webapi.fintrackautentication.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.webapi.fintrackautentication.domain.RefreshToken;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;
import com.example.webapi.fintrackautentication.helper.TestDataBuilder;
import com.example.webapi.fintrackautentication.helper.TokenTestHelper;
import com.example.webapi.fintrackautentication.repository.RefreshTokenRepository;
import com.example.webapi.fintrackautentication.security.JwtService;

/**
 * Tests para TokenServiceImpl.
 * Cubre: creación de tokens, refresh, revocación.
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User testUser;
    private RefreshToken validRefreshToken;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa datos comunes para todos los tests.
         */
        testUser = TestDataBuilder.createUserWithRoles("USER");
        validRefreshToken = TestDataBuilder.createValidRefreshToken(testUser);
    }

    @Test
    void createsTokensSuccessfully() {
        /*
         * Caso de éxito: genera access y refresh tokens válidos.
         * Esperado: retorna AuthenticationResponseDTO con ambos tokens.
         */
        String accessToken = TokenTestHelper.generateValidAccessToken(testUser.getEmail());
        String refreshToken = TokenTestHelper.generateValidRefreshToken(testUser.getEmail());

        when(refreshTokenRepository.findByUserId(testUser.getId()))
            .thenReturn(Collections.emptyList());
        when(jwtService.generateAccessToken(testUser.getEmail(), List.of("USER")))
            .thenReturn(accessToken);
        when(jwtService.generateRefreshToken(testUser.getEmail()))
            .thenReturn(refreshToken);
        when(jwtService.getRefreshTokenExpirationMs())
            .thenReturn(604800000L);
        when(jwtService.getAccessTokenExpirationMs())
            .thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenReturn(validRefreshToken);

        AuthenticationResponseDTO result = tokenService.createTokensForUser(testUser);

        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertEquals(testUser.getId(), result.getUserId());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void revokesOldTokensOnCreate() {
        /*
         * Verifica que los tokens anteriores se marquen como revocados.
         * Esperado: existentes se revocan, se crea uno nuevo.
         */
        RefreshToken oldToken = TestDataBuilder.createValidRefreshToken(testUser);
        List<RefreshToken> existingTokens = List.of(oldToken);

        String accessToken = TokenTestHelper.generateValidAccessToken(testUser.getEmail());
        String refreshToken = TokenTestHelper.generateValidRefreshToken(testUser.getEmail());

        when(refreshTokenRepository.findByUserId(testUser.getId()))
            .thenReturn(existingTokens);
        when(jwtService.generateAccessToken(testUser.getEmail(), List.of("USER")))
            .thenReturn(accessToken);
        when(jwtService.generateRefreshToken(testUser.getEmail()))
            .thenReturn(refreshToken);
        when(jwtService.getRefreshTokenExpirationMs())
            .thenReturn(604800000L);
        when(jwtService.getAccessTokenExpirationMs())
            .thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenReturn(validRefreshToken);

        tokenService.createTokensForUser(testUser);

        verify(refreshTokenRepository).save(oldToken);
        assertTrue(oldToken.isRevoked());
    }

    @Test
    void refreshesTokenSuccessfully() {
        /*
         * Caso de éxito: refresh token válido genera nuevos tokens.
         * Esperado: retorna nuevos access y refresh tokens.
         */
        String newAccessToken = TokenTestHelper.generateValidAccessToken(testUser.getEmail());
        String newRefreshToken = TokenTestHelper.generateValidRefreshToken(testUser.getEmail());

        when(refreshTokenRepository.findByToken(validRefreshToken.getToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateAccessToken(testUser.getEmail(), List.of("USER")))
            .thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(testUser.getEmail()))
            .thenReturn(newRefreshToken);
        when(jwtService.getRefreshTokenExpirationMs())
            .thenReturn(604800000L);
        when(jwtService.getAccessTokenExpirationMs())
            .thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenReturn(new RefreshToken());

        Optional<AuthenticationResponseDTO> result = tokenService.refresh(validRefreshToken.getToken());

        assertTrue(result.isPresent());
        assertNotNull(result.get().getAccessToken());
        assertNotNull(result.get().getRefreshToken());
        assertTrue(validRefreshToken.isRevoked());
    }

    @Test
    void rejectsNonexistentToken() {
        /*
         * Caso excepcional: token no existe en BD.
         * Esperado: retorna Optional.empty().
         */
        when(refreshTokenRepository.findByToken(anyString()))
            .thenReturn(Optional.empty());

        Optional<AuthenticationResponseDTO> result = tokenService.refresh("non_existent_token");

        assertTrue(result.isEmpty());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void rejectsRevokedToken() {
        /*
         * Caso excepcional: token ha sido revocado.
         * Esperado: retorna Optional.empty().
         */
        RefreshToken revokedToken = TestDataBuilder.createRevokedRefreshToken(testUser);

        when(refreshTokenRepository.findByToken(revokedToken.getToken()))
            .thenReturn(Optional.of(revokedToken));

        Optional<AuthenticationResponseDTO> result = tokenService.refresh(revokedToken.getToken());

        assertTrue(result.isEmpty());
    }

    @Test
    void rejectsExpiredToken() {
        /*
         * Caso excepcional: token ha expirado.
         * Esperado: retorna Optional.empty().
         */
        RefreshToken expiredToken = TestDataBuilder.createExpiredRefreshToken(testUser);

        when(refreshTokenRepository.findByToken(expiredToken.getToken()))
            .thenReturn(Optional.of(expiredToken));

        Optional<AuthenticationResponseDTO> result = tokenService.refresh(expiredToken.getToken());

        assertTrue(result.isEmpty());
    }

    @Test
    void revokesTokenByToken() {
        /*
         * Marca un token específico como revocado.
         * Esperado: token se marca con revoked=true.
         */
        when(refreshTokenRepository.findByToken(validRefreshToken.getToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenReturn(validRefreshToken);

        tokenService.revokeByToken(validRefreshToken.getToken());

        assertTrue(validRefreshToken.isRevoked());
        assertNotNull(validRefreshToken.getRevokedAt());
        verify(refreshTokenRepository).save(validRefreshToken);
    }

    @Test
    void rejectsRevokedTokenOnRevokeByToken() {
        /*
         * Caso excepcional: revocar un refresh token ya revocado.
         * Esperado: lanza InvalidRefreshTokenException.
         */
        RefreshToken alreadyRevoked = TestDataBuilder.createRevokedRefreshToken(testUser);
        when(refreshTokenRepository.findByToken(alreadyRevoked.getToken()))
            .thenReturn(Optional.of(alreadyRevoked));

        assertThrows(com.example.webapi.fintrackautentication.exception.InvalidRefreshTokenException.class, () -> {
            tokenService.revokeByToken(alreadyRevoked.getToken());
        });

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void rejectsExpiredTokenOnRevokeByToken() {
        /*
         * Caso excepcional: revocar un refresh token expirado.
         * Esperado: lanza InvalidRefreshTokenException.
         */
        RefreshToken expired = TestDataBuilder.createExpiredRefreshToken(testUser);
        when(refreshTokenRepository.findByToken(expired.getToken()))
            .thenReturn(Optional.of(expired));

        assertThrows(com.example.webapi.fintrackautentication.exception.InvalidRefreshTokenException.class, () -> {
            tokenService.revokeByToken(expired.getToken());
        });

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void revokesAllUserTokens() {

        /*
         * Revoca todos los tokens de un usuario.
         * Esperado: todos los tokens del usuario se marcan revocados.
         */
        RefreshToken token1 = TestDataBuilder.createValidRefreshToken(testUser);
        RefreshToken token2 = TestDataBuilder.createValidRefreshToken(testUser);
        List<RefreshToken> tokens = List.of(token1, token2);

        when(refreshTokenRepository.findByUserId(testUser.getId()))
            .thenReturn(tokens);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenReturn(new RefreshToken());

        tokenService.revokeAllForUser(testUser.getId());

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        assertTrue(token1.isRevoked());
        assertTrue(token2.isRevoked());
    }
}
