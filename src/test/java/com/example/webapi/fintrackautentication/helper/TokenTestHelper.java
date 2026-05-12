package com.example.webapi.fintrackautentication.helper;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para generar y validar JWT tokens en tests.
 */
public class TokenTestHelper {

    // Clave de 256 bits (32 bytes) en Base64 para pruebas
    private static final String JWT_SECRET = "dGVzdGtleWZvcmp3dHRhbGdvcml0aG0xMjM0NTY3ODkwYQ==";

    /**
     * Genera un access token válido para testing.
     */
    public static String generateValidAccessToken(String email) {
        /*
         * Genera un JWT válido con el email como subject y 15 minutos de expiración.
         */
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + 900000L);

        return Jwts.builder()
            .subject(email)
            .claim("roles", Collections.emptyList())
            .issuedAt(now)
            .expiration(expiresIn)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Genera un access token válido con roles.
     */
    public static String generateAccessTokenWithRoles(String email, List<String> roles) {
        /*
         * Genera un JWT válido con email, roles y expiración.
         */
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + 900000L);

        return Jwts.builder()
            .subject(email)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiresIn)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Genera un refresh token válido.
     */
    public static String generateValidRefreshToken(String email) {
        /*
         * Genera un JWT válido para refresh token con 7 días de expiración.
         */
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + 604800000L);

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiresIn)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Genera un token expirado.
     */
    public static String generateExpiredToken(String email) {
        /*
         * Genera un JWT que ya está expirado (fecha de expiración en el pasado).
         */
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() - 1000L);

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiresIn)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Genera un token con sujeto diferente.
     */
    public static String generateTokenForDifferentUser(String email) {
        /*
         * Genera un JWT con un email diferente al esperado.
         */
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + 900000L);

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiresIn)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Token inválido (sin firma válida).
     */
    public static String generateInvalidToken() {
        /*
         * Retorna una cadena que no es un JWT válido.
         */
        return "invalid.token.string";
    }

    /**
     * Extrae el subject (email) del token.
     */
    public static String extractSubject(String token) {
        /*
         * Decodifica el JWT y extrae el subject.
         */
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private static SecretKey getSigningKey() {
        /*
         * Obtiene la clave de firma para validar y crear JWTs.
         */
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
