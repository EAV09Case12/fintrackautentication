package com.example.webapi.fintrackautentication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

	@NotBlank
	@Size(max = 100)
	private String email;

	@NotBlank
	@Size(min = 8, max = 20)
	private String password;
}
