package com.example.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String title;

    @Valid
    private List<EmailDto> emails = new ArrayList<>();

    @Valid
    private List<PhoneDto> phones = new ArrayList<>();
}

