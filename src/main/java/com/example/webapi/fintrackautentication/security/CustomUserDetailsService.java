package com.example.webapi.fintrackautentication.security;

import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.domain.UserRol;
import com.example.webapi.fintrackautentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username)));

        java.util.List<SimpleGrantedAuthority> authorities = user.getUsuarioRoles() == null ? java.util.Collections.emptyList() : user.getUsuarioRoles().stream()
                .map(UserRol::getRol)
                .filter(java.util.Objects::nonNull)
                .map(r -> new SimpleGrantedAuthority(r.getNombre()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(!user.isEstado())
                .accountLocked(user.isCuentaBloqueada())
                .disabled(false)
                .build();
    }
}