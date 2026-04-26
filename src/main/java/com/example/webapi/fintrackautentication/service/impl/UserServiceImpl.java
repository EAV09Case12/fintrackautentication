package com.example.webapi.fintrackautentication.service.impl;

import com.example.webapi.fintrackautentication.domain.Rol;
import com.example.webapi.fintrackautentication.domain.User;
import com.example.webapi.fintrackautentication.domain.UserRol;
import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.UserResponseDTO;
import com.example.webapi.fintrackautentication.mapper.UserMapper;
import com.example.webapi.fintrackautentication.repository.RolRepository;
import com.example.webapi.fintrackautentication.repository.UserRepository;
import com.example.webapi.fintrackautentication.repository.UserRolRepository;
import com.example.webapi.fintrackautentication.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final UserRolRepository userRolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        validateEmailNotTaken(request.getEmail());
        User user = createUserFromRequest(request);
        user = userRepository.save(user);
        assignDefaultRole(user);
        return userMapper.toDto(user);
    }

    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new com.example.webapi.fintrackautentication.exception.EmailAlreadyExistsException("El correo ya está registrado");
        }
    }

    private User createUserFromRequest(RegisterRequestDTO request) {
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEstado(true);
        user.setCuentaBloqueada(false);
        user.setCuentaExpirada(false);
        user.setIntentosFallidos(0);
        return user;
    }

    private void assignDefaultRole(User user) {
        final String defaultRoleName = "USER";
        Rol rol = rolRepository.findByNombre(defaultRoleName)
                .orElseThrow(() -> new com.example.webapi.fintrackautentication.exception.RoleNotFoundException("Default role not configured: " + defaultRoleName));

        if (!userRolRepository.existsByUserIdAndRolId(user.getId(), rol.getId())) {
            UserRol ur = new UserRol();
            ur.setRol(rol);
            user.assignRole(ur);
            userRolRepository.save(ur);
        }
    }
}
