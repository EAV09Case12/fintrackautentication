package com.example.webapi.fintrackautentication.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * Tests simples para AuthController.
 * Validar anotaciones y métodos.
 */
class AuthControllerSimpleTest {

    @Test
    void controllerIsProperlyAnnotated() {
        /*
         * Verifica que el controller existe y puede ser instanciado.
         */
        assertNotNull(AuthController.class);
        assertTrue(AuthController.class.isAnnotationPresent(
            org.springframework.web.bind.annotation.RestController.class
        ));
    }

    @Test
    void hasRegisterMethod() {
        /*
         * Verifica que el controller tenga el método register.
         */
        try {
            AuthController.class.getDeclaredMethod("register",
                com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO.class);
        } catch (NoSuchMethodException e) {
            fail("Method register not found");
        }
    }

    @Test
    void hasLoginMethod() {
        /*
         * Verifica que el controller tenga el método login.
         */
        try {
            AuthController.class.getDeclaredMethod("login",
                com.example.webapi.fintrackautentication.dto.request.LoginRequestDTO.class);
        } catch (NoSuchMethodException e) {
            fail("Method login not found");
        }
    }

    @Test
    void hasRefreshMethod() {
        /*
         * Verifica que el controller tenga el método refresh.
         */
        try {
            AuthController.class.getDeclaredMethod("refresh",
                com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO.class);
        } catch (NoSuchMethodException e) {
            fail("Method refresh not found");
        }
    }

    @Test
    void hasLogoutMethod() {
        /*
         * Verifica que el controller tenga el método logout.
         */
        try {
            AuthController.class.getDeclaredMethod("logout",
                com.example.webapi.fintrackautentication.dto.request.RefreshTokenRequestDTO.class);
        } catch (NoSuchMethodException e) {
            fail("Method logout not found");
        }
    }
}
