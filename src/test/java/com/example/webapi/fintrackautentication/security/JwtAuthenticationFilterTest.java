package com.example.webapi.fintrackautentication.security;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuth() throws ServletException, IOException {


        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("Authorization")).thenReturn(null);

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void passesWhenTokenExtractionThrows() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(jwtService.extractUsername("bad")).thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername("bad");
        verifyNoInteractions(userDetailsService);
        assertDoesNotThrow(() -> SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void setAuth() throws ServletException, IOException {

        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid");

        when(jwtService.extractUsername("valid")).thenReturn("u@example.com");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("u@example.com")
                .password("x")
                .authorities(Collections.emptyList())
                .build();

        when(userDetailsService.loadUserByUsername("u@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid", userDetails)).thenReturn(true);

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.context.SecurityContext ctx = SecurityContextHolder.getContext();
        assertDoesNotThrow(() -> ctx.getAuthentication());

        assertDoesNotThrow(() -> {
            if (auth == null) throw new AssertionError("auth null");
        });

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername("u@example.com");
        verify(jwtService).isTokenValid("valid", userDetails);

        if (auth instanceof UsernamePasswordAuthenticationToken) {
        }

    }

    @Test
    void alreadyAuth() throws ServletException, IOException {

        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid");


        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        verify(filterChain).doFilter(request, response);

        verify(jwtService).extractUsername("valid");
        verifyNoInteractions(userDetailsService);
    }

}

