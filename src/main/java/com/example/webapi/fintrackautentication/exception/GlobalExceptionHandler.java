package com.example.webapi.fintrackautentication.exception;

import com.example.webapi.fintrackautentication.dto.response.MessageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;
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
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage() == null ? "Bad credentials" : ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<MessageResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		MessageResponseDTO body = new MessageResponseDTO(msg.isEmpty() ? "Validation failed" : msg);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<MessageResponseDTO> handleAll(Exception ex) {
		MessageResponseDTO body = new MessageResponseDTO(ex.getMessage() == null ? "Internal server error" : ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
