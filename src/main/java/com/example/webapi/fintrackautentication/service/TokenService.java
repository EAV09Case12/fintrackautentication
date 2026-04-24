package com.example.webapi.fintrackautentication.service;

import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;
import com.example.webapi.fintrackautentication.domain.User;

import java.util.Optional;

public interface TokenService {
	AuthenticationResponseDTO createTokensForUser(User user);
	Optional<AuthenticationResponseDTO> refresh(String refreshToken);
	void revokeByToken(String refreshToken);
	void revokeAllForUser(Long userId);
}
