package com.example.webapi.fintrackautentication.controller;

import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.UserResponseDTO;
import com.example.webapi.fintrackautentication.dto.request.LoginRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;
import com.example.webapi.fintrackautentication.service.AutenticacionService;
import com.example.webapi.fintrackautentication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name= "Autenticacion", description = "Gestión de autenticación de usuarios")
public class AuthController {

	private final UserService userService;
	private final AutenticacionService autenticacionService;

	@PostMapping("/register")
	@Operation(summary = "Registro", description = "Registrar un usuario en el sistema con email y contraseña")
	public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
		UserResponseDTO created = userService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PostMapping("/login")
	@Operation(summary = "Iniciar sesión", description = "Iniciar sesión con email y contraseña")
	public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
		AuthenticationResponseDTO tokens = autenticacionService.authenticate(request);
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Actualizar token", description = "Actualizar el token de acceso utilizando el token de refresco")
	public ResponseEntity<AuthenticationResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
		AuthenticationResponseDTO tokens = autenticacionService.refresh(request);
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/logout")
	@Operation(summary = "Cerrar sesión", description = "Cerrar sesión invalidando el token de refresco")
	public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
		autenticacionService.logout(request);
		return ResponseEntity.noContent().build();
	}
}
