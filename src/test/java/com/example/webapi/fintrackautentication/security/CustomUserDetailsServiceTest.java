package com.example.webapi.fintrackautentication.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.webapi.fintrackautentication.domain.Rol;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.domain.UserRol;
import com.example.webapi.fintrackautentication.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("u@example.com");
        user.setPasswordHash("hash");
        user.setEstado(true);
        user.setCuentaBloqueada(false);
        user.setUsuarioRoles(null);
    }

    @Test
    void returnsDetailsWhenFindByEmailWithRolesHasUser() {
        when(userRepository.findByEmailWithRoles("u@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("u@example.com");

        assertNotNull(details);
        assertEquals("u@example.com", details.getUsername());
        assertEquals("hash", details.getPassword());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isEnabled());
        assertEquals(Collections.emptyList(), details.getAuthorities().stream().toList());
    }

    @Test
    void usesFallbackFindByEmailWhenWithRolesEmptyAndFound() {
        when(userRepository.findByEmailWithRoles("u@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("u@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("u@example.com");

        assertNotNull(details);
        assertEquals("u@example.com", details.getUsername());
    }

    @Test
    void throwsWhenBothLookupsFail() {
        when(userRepository.findByEmailWithRoles("missing@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.com"));
    }

    @Test
    void mapsAuthoritiesWhenUsuarioRolesIsNotNull() {
        Rol rol = new Rol();
        rol.setNombre("USER");

        UserRol ur = new UserRol();
        ur.setRol(rol);

        user.setUsuarioRoles(new HashSet<>(Collections.singletonList(ur)));

        when(userRepository.findByEmailWithRoles("u@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("u@example.com");

        assertNotNull(details);
        assertEquals(1, details.getAuthorities().size());
        assertEquals("USER", details.getAuthorities().iterator().next().getAuthority());
    }
}

