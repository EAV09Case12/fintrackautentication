package com.example.webapi.fintrackautentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;

import java.util.Date;
import java.util.List;
import java.util.Collections;

@Service
public class JwtService {

	@Value("${jwt.secret:changeitchangeitchangeitchangeit}")
	private String jwtSecret;
	@Value("${jwt.access.expiration-ms:900000}")
	private long accessTokenExpirationMs;
	@Value("${jwt.refresh.expiration-ms:604800000}")
	private long refreshTokenExpirationMs;

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public String generateToken(String subject, long expirationMillis) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMillis);
		return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey()) 
                .compact();
    }

	public String generateAccessToken(String subject, List<String> roles, long expirationMillis) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMillis);
		return Jwts.builder()
                .subject(subject)
                .claim("roles", roles == null ? Collections.emptyList() : roles) 
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

	public String generateAccessToken(String subject, List<String> roles) {
        return generateAccessToken(subject, roles, accessTokenExpirationMs);
    }

	public String generateRefreshToken(String subject) {
		return generateToken(subject, refreshTokenExpirationMs);
	}

	public long getAccessTokenExpirationMs() {
		return accessTokenExpirationMs;
	}

	public long getRefreshTokenExpirationMs() {
		return refreshTokenExpirationMs;
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	private boolean isTokenExpired(String token) {
		final Date expiration = extractAllClaims(token).getExpiration();
		return expiration != null && expiration.before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
                .verifyWith(getSigningKey()) 
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

	private SecretKey getSigningKey() {
		try {
			byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (IllegalArgumentException ex) {
			// Not valid Base64 -> use raw UTF-8 bytes as fallback
			byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
			return Keys.hmacShaKeyFor(keyBytes);
		}
	}

}
