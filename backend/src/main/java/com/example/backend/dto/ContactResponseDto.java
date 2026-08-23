package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private List<EmailDto> emails = new ArrayList<>();
    private List<PhoneDto> phones = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

