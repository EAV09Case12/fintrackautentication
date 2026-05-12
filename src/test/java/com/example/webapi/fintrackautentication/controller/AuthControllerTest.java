package com.example.webapi.fintrackautentication.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.webapi.fintrackautentication.dto.request.LoginRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.AuthenticationResponseDTO;
import com.example.webapi.fintrackautentication.dto.response.UserResponseDTO;
import com.example.webapi.fintrackautentication.exception.EmailAlreadyExistsException;
import com.example.webapi.fintrackautentication.exception.GlobalExceptionHandler;
import com.example.webapi.fintrackautentication.exception.InvalidRefreshTokenException;
import com.example.webapi.fintrackautentication.exception.UserNotFoundException;
import com.example.webapi.fintrackautentication.helper.TestDataBuilder;
import com.example.webapi.fintrackautentication.helper.TokenTestHelper;
import com.example.webapi.fintrackautentication.service.AutenticacionService;
import com.example.webapi.fintrackautentication.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AutenticacionService autenticacionService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegisterRequestDTO validRegister;
    private LoginRequestDTO validLogin;
    private RefreshTokenRequestDTO refreshTokenRequest;
    private UserResponseDTO userResponse;
    private AuthenticationResponseDTO authResponse;

    @BeforeEach
    public void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        AuthController controller = new AuthController(userService, autenticacionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();

        objectMapper = new ObjectMapper().findAndRegisterModules();
        validRegister = TestDataBuilder.createValidRegisterRequest();
        validLogin = TestDataBuilder.createValidLoginRequest();
        refreshTokenRequest = TestDataBuilder.createValidRefreshTokenRequest();

        userResponse = new UserResponseDTO(
            1L,
            validRegister.getEmail(),
            true,
            false,
            null,
            null,
            null,
            List.of("USER")
        );

        authResponse = new AuthenticationResponseDTO(
            TokenTestHelper.generateValidAccessToken(validLogin.getEmail()),
            TokenTestHelper.generateValidRefreshToken(validLogin.getEmail()),
            "Bearer",
            900000L,
            1L,
            List.of("USER")
        );
    }

    @Test
    void registersUser() throws Exception {
        when(userService.register(any(RegisterRequestDTO.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegister)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(validRegister.getEmail()))
            .andExpect(jsonPath("$.estado").value(true));

        verify(userService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void rejectsExistingEmail() throws Exception {
        when(userService.register(any(RegisterRequestDTO.class)))
            .thenThrow(new EmailAlreadyExistsException("El correo ya esta registrado"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegister)))
            .andExpect(status().isConflict());

        verify(userService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void rejectsInvalidEmailFormat() throws Exception {
        RegisterRequestDTO invalidEmail = RegisterRequestDTO.builder()
            .email("invalid-email")
            .password("ValidPassword123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmail)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    @Test
    void rejectsWeakPassword() throws Exception {
        RegisterRequestDTO weakPassword = TestDataBuilder.createRegisterRequestWithWeakPassword();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPassword)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    @Test
    void rejectsMissingEmail() throws Exception {
        String jsonBody = "{\"password\":\"ValidPassword123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
            .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    @Test
    void authenticatesUser() throws Exception {
        when(autenticacionService.authenticate(any(LoginRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLogin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(autenticacionService).authenticate(any(LoginRequestDTO.class));
    }

    @Test
    void rejectsBadCredentials() throws Exception {
        when(autenticacionService.authenticate(any(LoginRequestDTO.class)))
            .thenThrow(new BadCredentialsException("credenciales invalidas"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLogin)))
            .andExpect(status().isUnauthorized());

        verify(autenticacionService).authenticate(any(LoginRequestDTO.class));
    }

    @Test
    void rejectsBlockedAccount() throws Exception {
        when(autenticacionService.authenticate(any(LoginRequestDTO.class)))
            .thenThrow(new BadCredentialsException("Cuenta bloqueada"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLogin)))
            .andExpect(status().isUnauthorized());

        verify(autenticacionService).authenticate(any(LoginRequestDTO.class));
    }

    @Test
    void rejectsUnknownUser() throws Exception {
        when(autenticacionService.authenticate(any(LoginRequestDTO.class)))
            .thenThrow(new UserNotFoundException("usuario no encontrado"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLogin)))
            .andExpect(status().isNotFound());

        verify(autenticacionService).authenticate(any(LoginRequestDTO.class));
    }

    @Test
    void rejectsMissingPassword() throws Exception {
        String jsonBody = "{\"password\":\"ValidPassword123\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
            .andExpect(status().isBadRequest());

        verify(autenticacionService, never()).authenticate(any());
    }

    @Test
    void refreshesToken() throws Exception {
        when(autenticacionService.refresh(any(RefreshTokenRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists());

        verify(autenticacionService).refresh(any(RefreshTokenRequestDTO.class));
    }

    @Test
    void rejectsInvalidRefreshToken() throws Exception {
        when(autenticacionService.refresh(any(RefreshTokenRequestDTO.class)))
            .thenThrow(new InvalidRefreshTokenException("Token de refresco invalido o expirado"));

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
            .andExpect(status().isForbidden());

        verify(autenticacionService).refresh(any(RefreshTokenRequestDTO.class));
    }

    @Test
    void logsoutUser() throws Exception {
        doNothing().when(autenticacionService).logout(any(RefreshTokenRequestDTO.class));

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
            .andExpect(status().isNoContent());

        verify(autenticacionService).logout(any(RefreshTokenRequestDTO.class));
    }

    @Test
    void rejectsLogoutWithMissingToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verify(autenticacionService, never()).logout(any());
    }
}
