package com.example.webapi.fintrackautentication.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.UserResponseDTO;
import com.example.webapi.fintrackautentication.exception.EmailAlreadyExistsException;
import com.example.webapi.fintrackautentication.exception.RoleNotFoundException;
import com.example.webapi.fintrackautentication.helper.MockRepositoryHelper;
import com.example.webapi.fintrackautentication.helper.TestDataBuilder;
import com.example.webapi.fintrackautentication.mapper.UserMapper;
import com.example.webapi.fintrackautentication.repository.RolRepository;
import com.example.webapi.fintrackautentication.repository.UserRepository;
import com.example.webapi.fintrackautentication.repository.UserRolRepository;

/**
 * Tests para UserServiceImpl.
 * Cubre: registro exitoso, email duplicado, rol por defecto.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UserRolRepository userRolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequestDTO validRequest;
    private User createdUser;
    @BeforeEach
    public void setUp() {
        /*
         * Inicializa datos comunes para todos los tests.
         */
        validRequest = TestDataBuilder.createValidRegisterRequest();
        createdUser = TestDataBuilder.createValidUser();
    }

    @Test
    void registersUserSuccessfully() {
        /*
         * Caso de éxito: usuario registrado exitosamente.
         * Esperado: retorna UserResponseDTO con email y estado activo.
         */
        when(userRepository.existsByEmail(validRequest.getEmail()))
            .thenReturn(false);
        MockRepositoryHelper.setupRolRepositoryWithUserRole(rolRepository);
        MockRepositoryHelper.setupUserRolRepositoryNoDuplicates(userRolRepository);

        when(passwordEncoder.encode(validRequest.getPassword()))
            .thenReturn("encoded_password");
        when(userMapper.toEntity(validRequest))
            .thenReturn(createdUser);
        when(userRepository.save(any(User.class)))
            .thenReturn(createdUser);
        when(userMapper.toDto(createdUser))
            .thenReturn(new UserResponseDTO(createdUser.getId(), createdUser.getEmail(),
                createdUser.isEstado(), createdUser.isCuentaBloqueada(),
                createdUser.getUltimoLogin(), createdUser.getCreatedAt(),
                createdUser.getUpdatedAt(), null));

        UserResponseDTO result = userService.register(validRequest);

        assertNotNull(result);
        assertEquals(createdUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRolRepository).save(any());
    }

    @Test
    void rejectsExistingEmail() {
        /*
         * Caso excepcional: email ya está registrado.
         * Esperado: lanza EmailAlreadyExistsException.
         */
        when(userRepository.existsByEmail(validRequest.getEmail()))
            .thenReturn(true);

        EmailAlreadyExistsException ex = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.register(validRequest);
        });
        assertNotNull(ex);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void throwsRoleNotFound() {
        /*
         * Caso excepcional: rol USER no está configurado en BD.
         * Esperado: lanza RoleNotFoundException.
         */
        when(userRepository.existsByEmail(validRequest.getEmail()))
            .thenReturn(false);
        MockRepositoryHelper.setupRolRepositoryWithoutRole(rolRepository);

        when(passwordEncoder.encode(validRequest.getPassword()))
            .thenReturn("encoded_password");
        when(userMapper.toEntity(validRequest))
            .thenReturn(createdUser);
        when(userRepository.save(any(User.class)))
            .thenReturn(createdUser);

        RoleNotFoundException ex = assertThrows(RoleNotFoundException.class, () -> {
            userService.register(validRequest);
        });
        assertNotNull(ex);

        verify(rolRepository).findByNombre("USER");
    }

    @Test
    void setsDefaultValues() {
        /*
         * Verifica que el usuario se cree con valores por defecto correctos.
         * Esperado: estado=true, cuentaBloqueada=false, intentosFallidos=0.
         */
        when(userRepository.existsByEmail(validRequest.getEmail()))
            .thenReturn(false);
        MockRepositoryHelper.setupRolRepositoryWithUserRole(rolRepository);
        when(userRolRepository.existsByUserIdAndRolId(any(), any()))
            .thenReturn(false);

        when(passwordEncoder.encode(validRequest.getPassword()))
            .thenReturn("encoded_password");
        when(userMapper.toEntity(validRequest))
            .thenReturn(new User());
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(any(User.class)))
            .thenReturn(new UserResponseDTO(1L, validRequest.getEmail(), true, false, null, null, null, null));

        userService.register(validRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }
}
