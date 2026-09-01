package com.example.backend.controller;

import com.example.backend.dto.ContactRequestDto;
import com.example.backend.dto.ContactResponseDto;
import com.example.backend.entity.User;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.UserRepository;
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
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<ContactResponseDto>> getAllContacts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        String userEmail = authentication.getName();
        log.info("GET /api/contacts requested by user '{}' (search='{}', page={}, size={})",
                userEmail, search, page, size);

        Page<ContactResponseDto> contacts = contactService.getAllContacts(userId, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDto> getContactById(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        String userEmail = authentication.getName();
        log.info("GET /api/contacts/{} requested by user '{}'", id, userEmail);

        ContactResponseDto contact = contactService.getContactById(userId, id);
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    public ResponseEntity<ContactResponseDto> createContact(
            @Valid @RequestBody ContactRequestDto request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        String userEmail = authentication.getName();
        log.info("POST /api/contacts requested by user '{}'", userEmail);

        ContactResponseDto created = contactService.createContact(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponseDto> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequestDto request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        String userEmail = authentication.getName();
        log.info("PUT /api/contacts/{} requested by user '{}'", id, userEmail);

        ContactResponseDto updated = contactService.updateContact(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        String userEmail = authentication.getName();
        log.info("DELETE /api/contacts/{} requested by user '{}'", id, userEmail);

        contactService.deleteContact(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Authentication is required");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
        return user.getId();
    }
}

