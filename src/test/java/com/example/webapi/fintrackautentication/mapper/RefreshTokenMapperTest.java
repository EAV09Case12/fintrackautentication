package com.example.webapi.fintrackautentication.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.webapi.fintrackautentication.domain.RefreshToken;
import com.example.webapi.fintrackautentication.dto.response.TokenResponseDTO;
import com.example.webapi.fintrackautentication.helper.TestDataBuilder;
import com.example.webapi.fintrackautentication.helper.TokenTestHelper;

/**
 * Tests para RefreshTokenMapper.
 * Cubre: mapeo de RefreshToken a DTO, manejo de null.
 */
class RefreshTokenMapperTest {

    private RefreshTokenMapper refreshTokenMapper;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa el mapper para cada test.
         */
        refreshTokenMapper = new RefreshTokenMapper();
    }

    @Test
    void toDtoMapsCorrectly() {
        /*
         * Caso de éxito: mapea RefreshToken a TokenResponseDTO.
         * Esperado: access token y refresh token se copian correctamente.
         */
        var user = TestDataBuilder.createValidUser();
        RefreshToken refreshToken = TestDataBuilder.createValidRefreshToken(user);
        String accessToken = TokenTestHelper.generateValidAccessToken(user.getEmail());

        TokenResponseDTO dto = refreshTokenMapper.toDto(refreshToken, accessToken);

        assertNotNull(dto);
        assertEquals(accessToken, dto.getAccessToken());
        assertEquals(refreshToken.getToken(), dto.getRefreshToken());
        assertEquals(refreshToken.getExpiresAt(), dto.getExpiresAt());
    }

    @Test
    void rejectsNullRefreshToken() {
        /*
         * Caso excepcional: refresh token es null.
         * Esperado: lanza NullPointerException.
         */
        String accessToken = TokenTestHelper.generateValidAccessToken("test@example.com");

        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            refreshTokenMapper.toDto(null, accessToken);
        });
        assertNotNull(ex);
    }

    @Test
    void rejectsNullAccessToken() {
        /*
         * Caso excepcional: access token es null.
         * Esperado: lanza NullPointerException.
         */
        var user = TestDataBuilder.createValidUser();
        RefreshToken refreshToken = TestDataBuilder.createValidRefreshToken(user);

        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            refreshTokenMapper.toDto(refreshToken, null);
        });
        assertNotNull(ex);
    }

    @Test
    void rejectsNullParameters() {
        /*
         * Caso excepcional: ambos parámetros son null.
         * Esperado: lanza NullPointerException.
         */
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            refreshTokenMapper.toDto(null, null);
        });
        assertNotNull(ex);
    }

    @Test
    void preservesExpiryDate() {
        /*
         * Verifica que la fecha de expiración se copia correctamente.
         * Esperado: el DTO tiene la misma fecha de expiración.
         */
        var user = TestDataBuilder.createValidUser();
        RefreshToken refreshToken = TestDataBuilder.createValidRefreshToken(user);
        String accessToken = TokenTestHelper.generateValidAccessToken(user.getEmail());

        TokenResponseDTO dto = refreshTokenMapper.toDto(refreshToken, accessToken);

        assertNotNull(dto.getExpiresAt());
        assertEquals(refreshToken.getExpiresAt(), dto.getExpiresAt());
    }
}
