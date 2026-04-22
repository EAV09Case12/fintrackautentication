package com.example.webapi.fintrackautentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

	@Value("${jwt.secret:changeitchangeitchangeitchangeit}")
	private String jwtSecret;

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
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
		Key key = getSigningKey();
		try {
			// Try modern API via reflection: Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody()
			Method parserBuilderM = Jwts.class.getMethod("parserBuilder");
			Object parserBuilder = parserBuilderM.invoke(null);
			Method setSigningKeyM = parserBuilder.getClass().getMethod("setSigningKey", Key.class);
			Object withKey = setSigningKeyM.invoke(parserBuilder, key);
			Method buildM = withKey.getClass().getMethod("build");
			Object parser = buildM.invoke(withKey);
			Method parseClaimsJwsM = parser.getClass().getMethod("parseClaimsJws", String.class);
			Object jws = parseClaimsJwsM.invoke(parser, token);
			Method getBodyM = jws.getClass().getMethod("getBody");
			return (Claims) getBodyM.invoke(jws);
		} catch (NoSuchMethodException ignored) {
			// Fall back to older API via reflection: Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody()
			try {
				Method parserM = Jwts.class.getMethod("parser");
				Object parserBuilder = parserM.invoke(null);
				Method setSigningKeyM = parserBuilder.getClass().getMethod("setSigningKey", Key.class);
				Object withKey = setSigningKeyM.invoke(parserBuilder, key);
				Method buildM = withKey.getClass().getMethod("build");
				Object parser = buildM.invoke(withKey);
				Method parseClaimsJwsM = parser.getClass().getMethod("parseClaimsJws", String.class);
				Object jws = parseClaimsJwsM.invoke(parser, token);
				Method getBodyM = jws.getClass().getMethod("getBody");
				return (Claims) getBodyM.invoke(jws);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to parse JWT via reflection", ex);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse JWT via reflection", e);
		}
	}

	private Key getSigningKey() {
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
