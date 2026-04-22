package com.example.webapi.fintrackautentication.exception;

import com.example.webapi.fintrackautentication.dto.response.MessageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
