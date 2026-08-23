package com.example.backend.controller;

import com.example.backend.dto.ContactRequestDto;
import com.example.backend.dto.ContactResponseDto;
import com.example.backend.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<Page<ContactResponseDto>> getAllContacts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("GET /api/contacts requested by user '{}' (search='{}', page={}, size={})",
                userEmail, search, page, size);

        Page<ContactResponseDto> contacts = contactService.getAllContacts(search, page, size, sortBy, sortDir, userEmail);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDto> getContactById(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("GET /api/contacts/{} requested by user '{}'", id, userEmail);

        ContactResponseDto contact = contactService.getContactById(id, userEmail);
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    public ResponseEntity<ContactResponseDto> createContact(
            @Valid @RequestBody ContactRequestDto request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("POST /api/contacts requested by user '{}'", userEmail);

        ContactResponseDto created = contactService.createContact(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponseDto> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequestDto request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("PUT /api/contacts/{} requested by user '{}'", id, userEmail);

        ContactResponseDto updated = contactService.updateContact(id, request, userEmail);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        log.info("DELETE /api/contacts/{} requested by user '{}'", id, userEmail);

        contactService.deleteContact(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}

