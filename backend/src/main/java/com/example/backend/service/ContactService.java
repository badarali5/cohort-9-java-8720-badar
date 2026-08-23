package com.example.backend.service;

import com.example.backend.dto.ContactRequestDto;
import com.example.backend.dto.ContactResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {

    /**
     * Fetch paginated contacts for the authenticated user
     */
    Page<ContactResponseDto> getContactsByUser(Long userId, Pageable pageable);

    /**
     * Search contacts by search term for the authenticated user
     */
    Page<ContactResponseDto> searchContacts(Long userId, String searchTerm, Pageable pageable);

    /**
     * Create a new contact with nested emails and phones
     */
    ContactResponseDto createContact(Long userId, ContactRequestDto contactRequestDto);

    /**
     * Update an existing contact
     */
    ContactResponseDto updateContact(Long userId, Long contactId, ContactRequestDto contactRequestDto);

    /**
     * Delete a contact
     */
    void deleteContact(Long userId, Long contactId);

    /**
     * Get a single contact by ID
     */
    ContactResponseDto getContactById(Long userId, Long contactId);
}
