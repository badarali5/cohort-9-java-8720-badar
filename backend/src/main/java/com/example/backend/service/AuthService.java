package com.example.backend.service;

import com.example.backend.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(Long userId, ChangePasswordRequest changePasswordRequest);
}

