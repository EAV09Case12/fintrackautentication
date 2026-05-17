package com.example.webapi.fintrackautentication.service.impl;

import com.example.webapi.fintrackautentication.dto.request.LoginRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;
import com.example.webapi.fintrackautentication.domain.AuditoriaAtenticacion;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.repository.AuditoriaRepository;
import com.example.webapi.fintrackautentication.repository.UserRepository;
import com.example.webapi.fintrackautentication.repository.RefreshTokenRepository;
import com.example.webapi.fintrackautentication.security.JwtService;
import com.example.webapi.fintrackautentication.service.AutenticacionService;
import com.example.webapi.fintrackautentication.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import com.example.webapi.fintrackautentication.exception.InvalidRefreshTokenException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticacionServiceImpl implements AutenticacionService {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtService jwtService;
	private final TokenService tokenService;
	private final AuditoriaRepository auditoriaRepository;

	private final com.example.webapi.fintrackautentication.config.SecurityProperties securityProperties;

	@Override
	@Transactional(noRollbackFor = BadCredentialsException.class)
	public AuthenticationResponseDTO authenticate(LoginRequestDTO request) {
		// Locate user first to manage attempt counters
		var userOpt = userRepository.findByEmail(request.getEmail());
		if (userOpt.isEmpty()) {
			// audit internally but do not reveal to client
			saveAudit(null, "LOGIN", request.getEmail(), false, "Usuario no encontrado");
			throw new BadCredentialsException("No fue posible iniciar sesión");
		}
		User user = userOpt.get();

		if (user.isCuentaBloqueada()) {
			saveAudit(user, "LOGIN", user.getEmail(), false, "Cuenta bloqueada");
			throw new BadCredentialsException("No fue posible iniciar sesión");
		}
		if (!user.isEstado()) {
			saveAudit(user, "LOGIN", user.getEmail(), false, "Cuenta inactiva");
			throw new BadCredentialsException("No fue posible iniciar sesión");
		}

		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

			// success: reset attempts, update last login, persist
			user.setIntentosFallidos(0);
			user.setUltimoLogin(java.time.LocalDateTime.now());
			user.setUpdatedAt(java.time.LocalDateTime.now());
			userRepository.save(user);

			// create tokens (TokenServiceImpl already revokes previous tokens)
			AuthenticationResponseDTO tokens = tokenService.createTokensForUser(user);
			saveAudit(user, "LOGIN", user.getEmail(), true, "Inicio de sesión exitoso");
			return tokens;
		} catch (BadCredentialsException ex) {
			// failed auth: increment attempts and possibly lock
			int intentos = user.getIntentosFallidos() + 1;
			user.setIntentosFallidos(intentos);
			if (intentos >= securityProperties.getMaxIntentosFallidos()) {
				user.setCuentaBloqueada(true);
			}
			user.setUpdatedAt(java.time.LocalDateTime.now());
			userRepository.save(user);

			if (user.isCuentaBloqueada()) {
				saveAudit(user, "LOGIN", user.getEmail(), false, "Cuenta bloqueada por intentos fallidos");
				throw new BadCredentialsException("No fue posible iniciar sesión");
			}

			saveAudit(user, "LOGIN", user.getEmail(), false, "Credenciales inválidas");
			throw new BadCredentialsException("No fue posible iniciar sesión");
		} catch (Exception ex) {
			saveAudit(user, "LOGIN", user.getEmail(), false, ex.getMessage());
			throw ex;
		}
	}

	@Override
	@Transactional(noRollbackFor = BadCredentialsException.class)
	public AuthenticationResponseDTO refresh(RefreshTokenRequestDTO request) {
		var opt = tokenService.refresh(request.getRefreshToken());
		if (opt.isPresent()) {
			// determine user for audit: try refresh token repository, fallback to extracting username from token
			var rtOpt = refreshTokenRepository.findByToken(request.getRefreshToken());
			User user = null;
			String email = null;
			if (rtOpt.isPresent()) {
				user = rtOpt.get().getUser();
				if (user != null) email = user.getEmail();
			} else {
				try { email = jwtService.extractUsername(request.getRefreshToken()); } catch (Exception ignored) {}
				if (email != null) user = userRepository.findByEmail(email).orElse(null);
			}

			saveAudit(user, "REFRESH_TOKEN", email, true, "Token de refresco renovado");
			return opt.get();
		}

		saveAudit(null, "REFRESH_TOKEN", null, false, "Token de refresco inválido o expirado");
		throw new InvalidRefreshTokenException("Token de refresco inválido o expirado");
	}

	@Override
	@Transactional(noRollbackFor = BadCredentialsException.class)
	public void logout(RefreshTokenRequestDTO request) {
		try {
			// find token first to preserve user info for audit (in case revoke deletes it)
			var rtOpt = refreshTokenRepository.findByToken(request.getRefreshToken());
			User user = null;
			String email = null;
			if (rtOpt.isPresent()) {
				user = rtOpt.get().getUser();
				if (user != null) email = user.getEmail();
			}

			tokenService.revokeByToken(request.getRefreshToken());

			saveAudit(user, "LOGOUT", email, true, "Cierre de sesión - token de refresco revocado");
		} catch (Exception ex) {
			saveAudit(null, "LOGOUT", null, false, ex.getMessage());
			throw ex;
		}
	}

	private void saveAudit(User user, String accion, String emailIntentado, boolean exito, String mensaje) {
		AuditoriaAtenticacion a = new AuditoriaAtenticacion();
		a.setUser(user);
		a.setAccion(accion);
		a.setEmailIntentado(emailIntentado);
		a.setExito(exito);
		a.setMensaje(mensaje);
		auditoriaRepository.save(a);
	}

}
