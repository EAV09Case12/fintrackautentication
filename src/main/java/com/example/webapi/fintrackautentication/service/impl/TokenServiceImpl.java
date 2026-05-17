package com.example.webapi.fintrackautentication.service.impl;

import com.example.webapi.fintrackautentication.domain.RefreshToken;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;
import com.example.webapi.fintrackautentication.repository.RefreshTokenRepository;
import com.example.webapi.fintrackautentication.security.JwtService;
import com.example.webapi.fintrackautentication.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import com.example.webapi.fintrackautentication.exception.InvalidRefreshTokenException;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AuthenticationResponseDTO createTokensForUser(User user) {
        // revoke existing tokens for user to avoid duplicates
        refreshTokenRepository.findByUserId(user.getId()).forEach(old -> {
            old.setRevoked(true);
            old.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(old);
        });

        List<String> roles = extractRoles(user);
        String access = jwtService.generateAccessToken(user.getEmail(), roles);
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken rt = new RefreshToken();
        rt.setToken(refresh);
        rt.setUser(user);
        rt.setRevoked(false);
        // compute expiresAt from refresh token expiration property
        long refreshMs = jwtService.getRefreshTokenExpirationMs();
        rt.setExpiresAt(LocalDateTime.now().plusNanos(refreshMs * 1_000_000));
        refreshTokenRepository.save(rt);

        AuthenticationResponseDTO resp = new AuthenticationResponseDTO();
        resp.setAccessToken(access);
        resp.setRefreshToken(refresh);
        resp.setExpiresIn(jwtService.getAccessTokenExpirationMs());
        resp.setUserId(user.getId());
        resp.setRoles(roles);
        // auditoría centralizada en AutenticacionServiceImpl (no se registra aquí)

        return resp;
    }

    @Override
    public Optional<AuthenticationResponseDTO> refresh(String refreshToken) {
        Optional<RefreshToken> existing = refreshTokenRepository.findByToken(refreshToken);
        if (existing.isEmpty()) return Optional.empty();
        RefreshToken rt = existing.get();
        if (rt.isRevoked()) return Optional.empty();
        if (rt.getExpiresAt() != null && rt.getExpiresAt().isBefore(LocalDateTime.now())) return Optional.empty();

        User user = rt.getUser();

        // rotate: revoke old, create new
        rt.setRevoked(true);
        rt.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(rt);

        List<String> roles = extractRoles(user);
        String newAccess = jwtService.generateAccessToken(user.getEmail(), roles);
        String newRefresh = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken newRt = new RefreshToken();
        newRt.setToken(newRefresh);
        newRt.setUser(user);
        newRt.setRevoked(false);
        long refreshMs = jwtService.getRefreshTokenExpirationMs();
        newRt.setExpiresAt(LocalDateTime.now().plusNanos(refreshMs * 1_000_000));
        refreshTokenRepository.save(newRt);

        AuthenticationResponseDTO resp = new AuthenticationResponseDTO();
        resp.setAccessToken(newAccess);
        resp.setRefreshToken(newRefresh);
        resp.setExpiresIn(jwtService.getAccessTokenExpirationMs());
        resp.setUserId(user.getId());
        resp.setRoles(roles);

        // auditoría centralizada en AutenticacionServiceImpl (no se registra aquí)

        return Optional.of(resp);
    }

    @Override
    public void revokeByToken(String refreshToken) {
        var existing = refreshTokenRepository.findByToken(refreshToken);
        if (existing.isEmpty()) {
            throw new InvalidRefreshTokenException("Token de refresco inválido o inexistente");
        }
        RefreshToken rt = existing.get();
        // No permitir revocar un token ya revocado o expirado
        if (rt.isRevoked() || (rt.getExpiresAt() != null && rt.getExpiresAt().isBefore(LocalDateTime.now()))) {
            throw new InvalidRefreshTokenException("Token de refresco inválido o expirado");
        }
        rt.setRevoked(true);
        rt.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(rt);
    }

    @Override
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.findByUserId(userId).forEach(rt -> {
            rt.setRevoked(true);
            rt.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(rt);
        });
    }

    private List<String> extractRoles(User user) {
        if (user == null || user.getUsuarioRoles() == null) return java.util.Collections.emptyList();
        return user.getUsuarioRoles().stream()
                .map(ur -> ur.getRol())
                .filter(java.util.Objects::nonNull)
                .map(r -> r.getNombre())
                .collect(Collectors.toList());
    }

}

