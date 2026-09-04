package com.example.backend.service;

import com.example.backend.dto.ContactRequestDto;
import com.example.backend.dto.ContactResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {
    Page<ContactResponseDto> getAllContacts(Long userId, String search, int page, int size, String sortBy, String sortDir);
    Page<ContactResponseDto> getContactsByUser(Long userId, Pageable pageable);
    Page<ContactResponseDto> searchContacts(Long userId, String searchTerm, Pageable pageable);
    ContactResponseDto createContact(Long userId, ContactRequestDto contactRequestDto);
    ContactResponseDto updateContact(Long userId, Long contactId, ContactRequestDto contactRequestDto);
    void deleteContact(Long userId, Long contactId);
    ContactResponseDto getContactById(Long userId, Long contactId);
    
}
