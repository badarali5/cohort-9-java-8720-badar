package com.example.backend.controller;

import com.example.backend.dto.ChangePasswordRequest;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserRepository userRepository;


    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Authentication is required");
        }

        String email = authentication.getName();
        log.info("Password change request received");

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.error("Authenticated user not found in database");
                    return new UnauthorizedException("Authentication is required");
                });

        authService.changePassword(user.getId(), request);
        log.info("Password changed successfully");

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}