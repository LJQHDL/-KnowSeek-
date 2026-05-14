package com.example.copilot.service;

import com.example.copilot.dto.request.LoginRequest;
import com.example.copilot.dto.request.RegisterRequest;
import com.example.copilot.dto.response.AuthResponse;

import com.example.copilot.entity.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse me(Long userId);

    AuthResponse refresh(Long userId);
}
