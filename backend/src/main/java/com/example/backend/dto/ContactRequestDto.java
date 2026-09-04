package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDto {

    private String firstName;
    private String lastName;
    private String title;
    private List<EmailDto> emails;
    private List<PhoneDto> phones;
}
