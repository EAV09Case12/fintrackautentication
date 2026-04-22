package com.example.webapi.fintrackautentication.service;

import com.example.webapi.fintrackautentication.dto.request.RegisterRequestDTO;
import com.example.webapi.fintrackautentication.dto.response.UserResponseDTO;

public interface UserService {
    UserResponseDTO register(RegisterRequestDTO request);
}
