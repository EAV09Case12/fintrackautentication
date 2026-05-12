package com.example.webapi.fintrackautentication.helper;

import java.time.LocalDateTime;

import com.example.webapi.fintrackautentication.domain.Permiso;
import com.example.webapi.fintrackautentication.domain.RefreshToken;
import com.example.webapi.fintrackautentication.domain.Rol;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.domain.UserRol;
import com.example.webapi.fintrackautentication.dto.request.LoginRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO;
import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;

/**
 * Constructor de datos de prueba siguiendo el patrón Builder.
 * Utilizado para evitar duplicación de código en tests.
 */
public class TestDataBuilder {

    public static User createValidUser() {
        /*
         * Crea un usuario válido con estado activo y cuenta no bloqueada.
         */
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password_123");
        user.setEstado(true);
        user.setCuentaBloqueada(false);
        user.setCuentaExpirada(false);
        user.setIntentosFallidos(0);
        user.setUltimoLogin(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUsuarioRoles(new java.util.HashSet<>());
        return user;
    }

    public static User createBlockedUser() {
        /*
         * Crea un usuario con cuenta bloqueada.
         */
        User user = createValidUser();
        user.setCuentaBloqueada(true);
        return user;
    }

    public static User createInactiveUser() {
        /*
         * Crea un usuario con estado inactivo.
         */
        User user = createValidUser();
        user.setEstado(false);
        return user;
    }

    public static User createUserWithFailedAttempts(int attempts) {
        /*
         * Crea un usuario con N intentos fallidos.
         */
        User user = createValidUser();
        user.setIntentosFallidos(attempts);
        return user;
    }

    public static Rol createUserRole() {
        /*
         * Crea el rol de usuario estándar.
         */
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("USER");
        rol.setCreatedAt(LocalDateTime.now());
        rol.setUpdatedAt(LocalDateTime.now());
        rol.setRolPermisos(new java.util.HashSet<>());
        return rol;
    }

    public static Rol createAdminRole() {
        /*
         * Crea el rol de administrador.
         */
        Rol rol = new Rol();
        rol.setId(2L);
        rol.setNombre("ADMIN");
        rol.setCreatedAt(LocalDateTime.now());
        rol.setUpdatedAt(LocalDateTime.now());
        rol.setRolPermisos(new java.util.HashSet<>());
        return rol;
    }

    public static Permiso createPermiso(String nombre) {
        /*
         * Crea un permiso con el nombre especificado.
         */
        Permiso permiso = new Permiso();
        permiso.setId(1L);
        permiso.setNombre(nombre);
        permiso.setCreatedAt(LocalDateTime.now());
        permiso.setUpdatedAt(LocalDateTime.now());
        return permiso;
    }

    public static UserRol createUserRol(User user, Rol rol) {
        /*
         * Crea una relación usuario-rol.
         */
        UserRol ur = new UserRol();
        ur.setId(1L);
        ur.setUser(user);
        ur.setRol(rol);
        ur.setCreatedAt(LocalDateTime.now());
        return ur;
    }

    public static RefreshToken createValidRefreshToken(User user) {
        /*
         * Crea un refresh token válido y no revocado.
         */
        RefreshToken rt = new RefreshToken();
        rt.setId(1L);
        rt.setToken("valid_refresh_token_jwt_string");
        rt.setUser(user);
        rt.setRevoked(false);
        rt.setExpiresAt(LocalDateTime.now().plusDays(7));
        rt.setCreatedAt(LocalDateTime.now());
        rt.setUpdatedAt(LocalDateTime.now());
        return rt;
    }

    public static RefreshToken createRevokedRefreshToken(User user) {
        /*
         * Crea un refresh token revocado.
         */
        RefreshToken rt = createValidRefreshToken(user);
        rt.setRevoked(true);
        rt.setRevokedAt(LocalDateTime.now());
        return rt;
    }

    public static RefreshToken createExpiredRefreshToken(User user) {
        /*
         * Crea un refresh token expirado.
         */
        RefreshToken rt = createValidRefreshToken(user);
        rt.setExpiresAt(LocalDateTime.now().minusDays(1));
        return rt;
    }

    public static RegisterRequestDTO createValidRegisterRequest() {
        /*
         * Crea una solicitud de registro válida.
         */
        return RegisterRequestDTO.builder()
            .email("newuser@example.com")
            .password("ValidPassword123")
            .build();
    }

    public static RegisterRequestDTO createRegisterRequestWithInvalidEmail() {
        /*
         * Crea una solicitud de registro con email inválido.
         */
        return RegisterRequestDTO.builder()
            .email("invalid-email")
            .password("ValidPassword123")
            .build();
    }

    public static RegisterRequestDTO createRegisterRequestWithWeakPassword() {
        /*
         * Crea una solicitud de registro con contraseña débil (falta mayúscula).
         */
        return RegisterRequestDTO.builder()
            .email("newuser@example.com")
            .password("weakpassword123")
            .build();
    }

    public static LoginRequestDTO createValidLoginRequest() {
        /*
         * Crea una solicitud de login válida.
         */
        return LoginRequestDTO.builder()
            .email("test@example.com")
            .password("ValidPassword123")
            .build();
    }

    public static LoginRequestDTO createLoginRequestWithInvalidEmail() {
        /*
         * Crea una solicitud de login con email inválido.
         */
        return LoginRequestDTO.builder()
            .email("")
            .password("ValidPassword123")
            .build();
    }

    public static RefreshTokenRequestDTO createValidRefreshTokenRequest() {
        /*
         * Crea una solicitud de refresh token válida.
         */
        return RefreshTokenRequestDTO.builder()
            .refreshToken("valid_refresh_token_string")
            .build();
    }

    public static RefreshTokenRequestDTO createInvalidRefreshTokenRequest() {
        /*
         * Crea una solicitud de refresh token sin token.
         */
        return RefreshTokenRequestDTO.builder()
            .refreshToken("")
            .build();
    }

    public static User createUserWithRoles(String... roleNames) {
        /*
         * Crea un usuario con roles específicos.
         */
        User user = createValidUser();
        java.util.Set<UserRol> roles = new java.util.HashSet<>();
        for (String roleName : roleNames) {
            Rol rol = new Rol();
            rol.setNombre(roleName);
            UserRol ur = new UserRol();
            ur.setRol(rol);
            roles.add(ur);
        }
        user.setUsuarioRoles(roles);
        return user;
    }
}
