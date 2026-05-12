package com.example.webapi.fintrackautentication.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.domain.UserRol;
import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.UserResponseDTO;
import com.example.webapi.fintrackautentication.helper.TestDataBuilder;

/**
 * Tests para UserMapper.
 * Cubre: mapeo de User a DTO y viceversa, manejo de roles.
 */
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        /*
         * Inicializa el mapper para cada test.
         */
        userMapper = new UserMapper();
    }

    @Test
    void toDtoMapsUserCorrectly() {
        /*
         * Caso de éxito: mapea User a UserResponseDTO correctamente.
         * Esperado: todos los campos coinciden.
         */
        User user = TestDataBuilder.createValidUser();
        UserRol userRol = TestDataBuilder.createUserRol(user, TestDataBuilder.createUserRole());
        user.getUsuarioRoles().add(userRol);

        UserResponseDTO dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.isEstado(), dto.isEstado());
        assertEquals(user.isCuentaBloqueada(), dto.isCuentaBloqueada());
        assertNotNull(dto.getRoles());
        assertTrue(dto.getRoles().contains("USER"));
    }

    @Test
    void toDtoIncludesAllRoles() {
        /*
         * Mapea usuario con múltiples roles.
         * Esperado: todos los roles se incluyen en el DTO.
         */
        User user = TestDataBuilder.createUserWithRoles("USER", "ADMIN");

        UserResponseDTO dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertNotNull(dto.getRoles());
        assertEquals(2, dto.getRoles().size());
        assertTrue(dto.getRoles().contains("USER"));
        assertTrue(dto.getRoles().contains("ADMIN"));
    }

    @Test
    void toDtoHandlesNullUser() {
        /*
         * Mapea null a null sin lanzar excepciones.
         * Esperado: retorna null.
         */
        UserResponseDTO dto = userMapper.toDto(null);

        assertNull(dto);
    }

    @Test
    void toDtoHandlesNullRoles() {
        /*
         * Mapea usuario sin roles.
         * Esperado: el DTO tiene lista de roles vacía o null.
         */
        User user = TestDataBuilder.createValidUser();
        user.setUsuarioRoles(null);

        UserResponseDTO dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertNull(dto.getRoles());
    }

    @Test
    void toDtoHandlesEmptyRoles() {
        /*
         * Mapea usuario con lista de roles vacía.
         * Esperado: el DTO tiene lista de roles vacía.
         */
        User user = TestDataBuilder.createValidUser();
        user.setUsuarioRoles(new java.util.HashSet<>());

        UserResponseDTO dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertNotNull(dto.getRoles());
        assertTrue(dto.getRoles().isEmpty());
    }

    @Test
    void toEntityMapsRegisterDto() {
        /*
         * Caso de éxito: mapea RegisterRequestDTO a User.
         * Esperado: email se copia correctamente.
         */
        RegisterRequestDTO dto = TestDataBuilder.createValidRegisterRequest();

        User user = userMapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(dto.getEmail(), user.getEmail());
        assertNull(user.getPasswordHash());
    }

    @Test
    void toEntityHandlesNullDto() {
        /*
         * Mapea null a null sin lanzar excepciones.
         * Esperado: retorna null.
         */
        User user = userMapper.toEntity(null);

        assertNull(user);
    }

    @Test
    void toEntityDoesNotMapPassword() {
        /*
         * Verifica que el mapper NO establece la contraseña (responsabilidad del service).
         * Esperado: passwordHash es null.
         */
        RegisterRequestDTO dto = TestDataBuilder.createValidRegisterRequest();

        User user = userMapper.toEntity(dto);

        assertNotNull(user);
        assertNull(user.getPasswordHash());
    }
}
