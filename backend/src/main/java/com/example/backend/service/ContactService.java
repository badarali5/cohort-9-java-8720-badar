package com.example.backend.service;

import com.example.backend.dto.ContactRequestDto;
import com.example.backend.dto.ContactResponseDto;
import org.springframework.data.domain.Page;

public interface ContactService {

    Page<ContactResponseDto> getAllContacts(String search, int page, int size, String sortBy, String sortDir, String userEmail);

    ContactResponseDto getContactById(Long id, String userEmail);

    ContactResponseDto createContact(ContactRequestDto request, String userEmail);

    ContactResponseDto updateContact(Long id, ContactRequestDto request, String userEmail);

    void deleteContact(Long id, String userEmail);
}

