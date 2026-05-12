package com.example.webapi.fintrackautentication.helper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.webapi.fintrackautentication.domain.Rol;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.repository.RolRepository;
import com.example.webapi.fintrackautentication.repository.UserRepository;
import com.example.webapi.fintrackautentication.repository.UserRolRepository;

/**
 * Utilidad para configurar mocks comunes de repositorios.
 * Reutilizable en múltiples tests para evitar duplicación.
 */
public class MockRepositoryHelper {

    /**
     * Configura el UserRepository para encontrar un usuario válido.
     */
    public static void setupUserRepositoryWithValidUser(UserRepository userRepository, User user) {
        /*
         * Configura el mock para devolver el usuario cuando se busca por email.
         */
        when(userRepository.findByEmail(user.getEmail()))
            .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(user.getEmail()))
            .thenReturn(true);
    }

    /**
     * Configura el UserRepository para usuario no encontrado.
     */
    public static void setupUserRepositoryWithoutUser(UserRepository userRepository, String email) {
        /*
         * Configura el mock para devolver vacío cuando el usuario no existe.
         */
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.empty());
        when(userRepository.existsByEmail(email))
            .thenReturn(false);
    }

    /**
     * Configura el RolRepository para el rol USER.
     */
    public static void setupRolRepositoryWithUserRole(RolRepository rolRepository) {
        /*
         * Configura el mock para devolver el rol USER cuando se busca.
         */
        Rol userRole = TestDataBuilder.createUserRole();
        when(rolRepository.findByNombre("USER"))
            .thenReturn(Optional.of(userRole));
    }

    /**
     * Configura el RolRepository para rol no encontrado.
     */
    public static void setupRolRepositoryWithoutRole(RolRepository rolRepository) {
        /*
         * Configura el mock para devolver vacío cuando el rol no existe.
         */
        when(rolRepository.findByNombre(anyString()))
            .thenReturn(Optional.empty());
    }

    /**
     * Configura UserRolRepository para verificar duplicados.
     */
    public static void setupUserRolRepositoryNoDuplicates(UserRolRepository userRolRepository) {
        /*
         * Configura el mock para indicar que no hay duplicados, con cualquier ID.
         */
        when(userRolRepository.existsByUserIdAndRolId(anyLong(), anyLong()))
            .thenReturn(false);
    }
}
