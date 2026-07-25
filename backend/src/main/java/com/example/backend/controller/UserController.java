package com.example.backend.controller;

import com.example.backend.dto.ChangePasswordRequest;
import com.example.backend.entity.User;
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

    /**
     * Changes the password for the currently authenticated user.
     *
     * @param request the current and new password payload
     * @param authentication the current authenticated session
     * @return success message
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("Password change request received for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Authenticated user not found in database: {}", email);
                    return new RuntimeException("User not found");
                });

        authService.changePassword(user.getId(), request);
        log.info("Password changed successfully for user: {}", email);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}

