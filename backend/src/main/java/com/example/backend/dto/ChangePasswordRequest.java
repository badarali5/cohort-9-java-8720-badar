package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "New password is required")
@Size(max = 72, message = "New password must not exceed 72 characters")
@Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,72}$",
        message = "Password must be 8-72 characters and contain at least one letter and one number"
)
private String newPassword;
}

