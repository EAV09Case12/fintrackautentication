package com.example.webapi.fintrackautentication.mapper;

import com.example.webapi.fintrackautentication.domain.RefreshToken;
import com.example.webapi.fintrackautentication.dto.response.TokenResponseDTO;

import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    /**
     * Map a RefreshToken entity to a TokenResponseDTO.
     * @param token persisted refresh token
     * @param accessToken generated access token (JWT)
     */
    public TokenResponseDTO toDto(RefreshToken token, String accessToken) {
        Objects.requireNonNull(token, "RefreshToken no puede ser null");
        Objects.requireNonNull(accessToken, "AccessToken no puede ser null");
        TokenResponseDTO dto = new TokenResponseDTO();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(token.getToken());
        dto.setExpiresAt(token.getExpiresAt());
        return dto;
    }
}
