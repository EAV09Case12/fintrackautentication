package com.example.webapi.fintrackautentication.mapper;

import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.dto.response.UserResponseDTO;
import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toDto(User user) {
        if (user == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setEstado(user.isEstado());
        dto.setCuentaBloqueada(user.isCuentaBloqueada());
        dto.setUltimoLogin(user.getUltimoLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        // map roles if present
        if (user.getUsuarioRoles() != null) {
            List<String> roles = user.getUsuarioRoles().stream()
                .filter(Objects::nonNull)
                .map(ur -> ur.getRol())
                .filter(Objects::nonNull)
                .map(r -> r.getNombre())
                .collect(Collectors.toList());
            dto.setRoles(roles);
        }
        return dto;
    }

    /**
     * Create a minimal User entity from registration request.
     * Note: do NOT set business defaults (estado, flags) here — service layer should apply those.
     * Also, the service is responsible for encoding and setting the passwordHash.
     */
    public User toEntity(RegisterRequestDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setEmail(dto.getEmail());
        return user;
    }
}
