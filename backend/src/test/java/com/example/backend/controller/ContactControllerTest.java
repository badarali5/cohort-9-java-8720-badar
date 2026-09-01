package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    @Mock
    private ContactService contactService;

    @Mock
    private UserRepository userRepository;

    private ContactController contactController;

    @BeforeEach
    void setUp() {
        contactController = new ContactController(contactService, userRepository);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("Should reject negative page values")
    void shouldRejectNegativePage() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password", List.of());

        when(contactService.getAllContacts(1L, null, -1, 10, "firstName", "asc"))
                .thenThrow(new IllegalArgumentException("Page index must be >= 0"));

        assertThrows(IllegalArgumentException.class, () ->
                contactController.getAllContacts(null, -1, 10, "firstName", "asc", authentication));
    }

    @Test
    @DisplayName("Should reject invalid page sizes")
    void shouldRejectInvalidPageSize() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password", List.of());

        when(contactService.getAllContacts(1L, null, 0, 0, "firstName", "asc"))
                .thenThrow(new IllegalArgumentException("Page size must be greater than 0"));

        assertThrows(IllegalArgumentException.class, () ->
                contactController.getAllContacts(null, 0, 0, "firstName", "asc", authentication));
    }

    @Test
    @DisplayName("Should reject unknown sort fields")
    void shouldRejectUnknownSortField() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password", List.of());

        when(contactService.getAllContacts(1L, null, 0, 10, "unknownField", "asc"))
                .thenThrow(new IllegalArgumentException("Invalid sort field. Allowed values: id, firstName, lastName, email, phone"));

        assertThrows(IllegalArgumentException.class, () ->
                contactController.getAllContacts(null, 0, 10, "unknownField", "asc", authentication));
    }
}
