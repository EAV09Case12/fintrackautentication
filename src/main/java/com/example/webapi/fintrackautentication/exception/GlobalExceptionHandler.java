package com.example.webapi.fintrackautentication.exception;

import com.example.webapi.fintrackautentication.dto.response.MessageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<MessageResponseDTO> handleEmailExists(EmailAlreadyExistsException ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<MessageResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
		MessageResponseDTO body = new MessageResponseDTO("El correo ya está registrado");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(RoleNotFoundException.class)
	public ResponseEntity<MessageResponseDTO> handleRoleNotFound(RoleNotFoundException ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<MessageResponseDTO> handleIllegalArg(IllegalArgumentException ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<MessageResponseDTO> handleBadCredentials(BadCredentialsException ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage() == null ? "credenciales inválidas" : ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<MessageResponseDTO> handleUserNotFound(UserNotFoundException ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage() == null ? "usuario no encontrado" : ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<MessageResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		MessageResponseDTO body = new MessageResponseDTO(msg.isEmpty() ? "Validación fallida" : msg);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<MessageResponseDTO> handleAll(Exception ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage() == null ? "Internal server error" : ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<MessageResponseDTO> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}
}
