package com.example.webapi.fintrackautentication.service;

import com.example.webapi.fintrackautentication.dto.request.LoginRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;

public interface AutenticacionService {
	AuthenticationResponseDTO authenticate(LoginRequestDTO request);
	AuthenticationResponseDTO refresh(RefreshTokenRequestDTO request);
	void logout(RefreshTokenRequestDTO request);
}
