package com.example.backend.service;

import com.example.backend.dto.ContactRequestDto;
import com.example.backend.dto.ContactResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface ContactService {

    Page<ContactResponseDto> getAllContacts(String search, int page, int size, String sortBy, String sortDir, String userEmail);

    ContactResponseDto getContactById(Long id, String userEmail);

    ContactResponseDto createContact(ContactRequestDto request, String userEmail);

    ContactResponseDto updateContact(Long id, ContactRequestDto request, String userEmail);

    void deleteContact(Long id, String userEmail);

    Page<ContactResponseDto> getContactsByUser(Long userId, Pageable pageable);

    Page<ContactResponseDto> searchContacts(Long userId, String searchTerm, Pageable pageable);

    ContactResponseDto createContact(Long userId, ContactRequestDto request);

    ContactResponseDto updateContact(Long userId, Long contactId, ContactRequestDto request);

    void deleteContact(Long userId, Long contactId);

    ContactResponseDto getContactById(Long userId, Long contactId);
}

