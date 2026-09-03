package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.Contact;
import com.example.backend.entity.Email;
import com.example.backend.entity.Phone;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedAccessException;
import com.example.backend.repository.ContactRepository;
import com.example.backend.repository.EmailRepository;
import com.example.backend.repository.PhoneRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Contact Service Tests")
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private PhoneRepository phoneRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private User user;
    private Contact contact;
    private ContactRequestDto contactRequestDto;
    private Email email;
    private Phone phone;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");

        contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("Jane");
        contact.setLastName("Smith");
        contact.setTitle("Developer");
        contact.setUser(user);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());

        email = new Email();
        email.setId(1L);
        email.setEmail("jane.smith@example.com");
        email.setLabel("Work");
        email.setContact(contact);

        phone = new Phone();
        phone.setId(1L);
        phone.setNumber("+1234567890");
        phone.setLabel("Mobile");
        phone.setContact(contact);

        contact.setEmails(Arrays.asList(email));
        contact.setPhones(Arrays.asList(phone));

        contactRequestDto = new ContactRequestDto();
        contactRequestDto.setFirstName("Jane");
        contactRequestDto.setLastName("Smith");
        contactRequestDto.setTitle("Developer");
        contactRequestDto.setEmails(Arrays.asList(
                new EmailDto(null, "jane.smith@example.com", "Work")
        ));
        contactRequestDto.setPhones(Arrays.asList(
                new PhoneDto(null, "+1234567890", "Mobile")
        ));

        pageable = PageRequest.of(0, 10);
    }

    // ==================== Get Contacts Tests ====================

    @Test
    @DisplayName("Should fetch paginated contacts for the logged-in user")
    void testGetContactsByUser() {
        // Arrange
        Contact contact2 = new Contact();
        contact2.setId(2L);
        contact2.setFirstName("John");
        contact2.setLastName("Developer");
        contact2.setUser(user);

        List<Contact> contacts = Arrays.asList(contact, contact2);
        Page<Contact> contactPage = new PageImpl<>(contacts, pageable, 2);

        when(contactRepository.findByUserId(user.getId(), pageable)).thenReturn(contactPage);

        // Act
        Page<ContactResponseDto> result = contactService.getContactsByUser(user.getId(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Jane", result.getContent().get(0).getFirstName());
        assertEquals("John", result.getContent().get(1).getFirstName());

        // Verify
        verify(contactRepository).findByUserId(user.getId(), pageable);
    }

    @Test
    @DisplayName("Should return empty page when user has no contacts")
    void testGetContactsByUserEmpty() {
        // Arrange
        Page<Contact> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        when(contactRepository.findByUserId(user.getId(), pageable)).thenReturn(emptyPage);

        // Act
        Page<ContactResponseDto> result = contactService.getContactsByUser(user.getId(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());

        // Verify
        verify(contactRepository).findByUserId(user.getId(), pageable);
    }

    // ==================== Search Contacts Tests ====================

    @Test
    @DisplayName("Should search contacts by term returning correct filtered records")
    void testSearchContactsByTerm() {
        // Arrange
        String searchTerm = "Developer";
        Contact searchContact = new Contact();
        searchContact.setId(1L);
        searchContact.setFirstName("Jane");
        searchContact.setLastName("Developer");
        searchContact.setTitle("Developer");
        searchContact.setUser(user);

        List<Contact> searchResults = Arrays.asList(searchContact);
        Page<Contact> searchPage = new PageImpl<>(searchResults, pageable, 1);

        when(contactRepository.searchContacts(user.getId(), searchTerm, pageable)).thenReturn(searchPage);

        // Act
        Page<ContactResponseDto> result = contactService.searchContacts(user.getId(), searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Developer", result.getContent().get(0).getLastName());

        // Verify
        verify(contactRepository).searchContacts(user.getId(), searchTerm, pageable);
    }

    @Test
    @DisplayName("Should return empty results when search term matches no contacts")
    void testSearchContactsNoResults() {
        // Arrange
        String searchTerm = "NonExistent";
        Page<Contact> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(contactRepository.searchContacts(user.getId(), searchTerm, pageable)).thenReturn(emptyPage);

        // Act
        Page<ContactResponseDto> result = contactService.searchContacts(user.getId(), searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());

        // Verify
        verify(contactRepository).searchContacts(user.getId(), searchTerm, pageable);
    }

    // ==================== Create Contact Tests ====================

    @Test
    @DisplayName("Should create a contact with nested emails and phones")
    void testCreateContactSuccess() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);
        when(emailRepository.saveAll(anyList())).thenReturn(contact.getEmails());
        when(phoneRepository.saveAll(anyList())).thenReturn(contact.getPhones());

        // Act
        ContactResponseDto result = contactService.createContact(user.getId(), contactRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Developer", result.getTitle());
        assertEquals(1, result.getEmails().size());
        assertEquals(1, result.getPhones().size());
        assertEquals("jane.smith@example.com", result.getEmails().get(0).getEmail());
        assertEquals("+1234567890", result.getPhones().get(0).getNumber());

        // Verify
        verify(userRepository).findById(user.getId());
        verify(contactRepository).save(any(Contact.class));
        verify(emailRepository).saveAll(anyList());
        verify(phoneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should create a contact without emails and phones")
    void testCreateContactWithoutEmailsAndPhones() {
        // Arrange
        ContactRequestDto requestDto = new ContactRequestDto();
        requestDto.setFirstName("Jane");
        requestDto.setLastName("Smith");
        requestDto.setTitle("Developer");
        requestDto.setEmails(new ArrayList<>());
        requestDto.setPhones(new ArrayList<>());

        Contact contactWithoutEmailsPhones = new Contact();
        contactWithoutEmailsPhones.setId(1L);
        contactWithoutEmailsPhones.setFirstName("Jane");
        contactWithoutEmailsPhones.setLastName("Smith");
        contactWithoutEmailsPhones.setTitle("Developer");
        contactWithoutEmailsPhones.setUser(user);
        contactWithoutEmailsPhones.setEmails(new ArrayList<>());
        contactWithoutEmailsPhones.setPhones(new ArrayList<>());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(contactRepository.save(any(Contact.class))).thenReturn(contactWithoutEmailsPhones);

        // Act
        ContactResponseDto result = contactService.createContact(user.getId(), requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertTrue(result.getEmails().isEmpty());
        assertTrue(result.getPhones().isEmpty());

        // Verify
        verify(userRepository).findById(user.getId());
        verify(contactRepository).save(any(Contact.class));
        verify(emailRepository, never()).saveAll(anyList());
        verify(phoneRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found during contact creation")
    void testCreateContactUserNotFound() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> contactService.createContact(user.getId(), contactRequestDto)
        );

        assertEquals("User not found", exception.getMessage());

        // Verify
        verify(userRepository).findById(user.getId());
        verify(contactRepository, never()).save(any(Contact.class));
    }

    // ==================== Update Contact Tests ====================

    @Test
    @DisplayName("Should update a contact when it belongs to the authenticated user")
    void testUpdateContactSuccess() {
        // Arrange
        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.of(contact));
        when(emailRepository.saveAll(anyList())).thenReturn(contact.getEmails());
        when(phoneRepository.saveAll(anyList())).thenReturn(contact.getPhones());
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        // Act
        ContactResponseDto result = contactService.updateContact(user.getId(), contact.getId(), contactRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());

        // Verify
        verify(contactRepository).findByIdAndUserId(contact.getId(), user.getId());
        verify(emailRepository).deleteAll(anyList());
        verify(phoneRepository).deleteAll(anyList());
        verify(emailRepository).saveAll(anyList());
        verify(phoneRepository).saveAll(anyList());
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when updating a contact owned by another user")
    void testUpdateContactUnauthorized() {
        // Arrange
        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> contactService.updateContact(user.getId(), contact.getId(), contactRequestDto)
        );

        assertEquals("You do not have permission to update this contact", exception.getMessage());

        // Verify
        verify(contactRepository).findByIdAndUserId(contact.getId(), user.getId());
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    @DisplayName("Should update contact without modifying emails and phones when not provided")
    void testUpdateContactWithoutEmailsPhones() {
        // Arrange
        ContactRequestDto requestDto = new ContactRequestDto();
        requestDto.setFirstName("UpdatedFirstName");
        requestDto.setLastName("UpdatedLastName");
        requestDto.setTitle("UpdatedTitle");
        requestDto.setEmails(null);
        requestDto.setPhones(null);

        Contact updatedContact = new Contact();
        updatedContact.setId(contact.getId());
        updatedContact.setFirstName("UpdatedFirstName");
        updatedContact.setLastName("UpdatedLastName");
        updatedContact.setTitle("UpdatedTitle");
        updatedContact.setUser(user);
        updatedContact.setEmails(contact.getEmails());
        updatedContact.setPhones(contact.getPhones());

        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(updatedContact);

        // Act
        ContactResponseDto result = contactService.updateContact(user.getId(), contact.getId(), requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("UpdatedFirstName", result.getFirstName());

        // Verify
        verify(contactRepository).findByIdAndUserId(contact.getId(), user.getId());
        verify(emailRepository, never()).deleteAll(anyList());
        verify(phoneRepository, never()).deleteAll(anyList());
        verify(contactRepository).save(any(Contact.class));
    }

    // ==================== Delete Contact Tests ====================

    @Test
    @DisplayName("Should delete a contact when it belongs to the authenticated user")
    void testDeleteContactSuccess() {
        // Arrange
        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.of(contact));

        // Act
        contactService.deleteContact(user.getId(), contact.getId());

        // Assert - No exception thrown

        // Verify
        verify(contactRepository).findByIdAndUserId(contact.getId(), user.getId());
        verify(contactRepository).delete(contact);
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when deleting a contact owned by another user")
    void testDeleteContactUnauthorized() {
        // Arrange
        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> contactService.deleteContact(user.getId(), contact.getId())
        );

        assertEquals("You do not have permission to delete this contact", exception.getMessage());

        // Verify
        verify(contactRepository).findByIdAndUserId(contact.getId(), user.getId());
        verify(contactRepository, never()).delete(any(Contact.class));
    }

    // ==================== Get Contact By ID Tests ====================

    @Test
    @DisplayName("Should fetch a single contact by ID for the authenticated user")
    void testGetContactByIdSuccess() {
        // Arrange
        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.of(contact));

        // Act
        ContactResponseDto result = contactService.getContactById(user.getId(), contact.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Developer", result.getTitle());
        assertEquals(1, result.getEmails().size());
        assertEquals(1, result.getPhones().size());

        // Verify
        verify(contactRepository).findByIdAndUserId(contact.getId(), user.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when contact not found")
    void testGetContactByIdNotFound() {
        // Arrange
        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> contactService.getContactById(user.getId(), contact.getId())
        );

        assertEquals("Contact not found", exception.getMessage());

        // Verify
        verify(contactRepository).findByIdAndUserId(contact.getId(), user.getId());
    }

    // ==================== DTO Conversion Tests ====================

    @Test
    @DisplayName("Should correctly map contact entity to response DTO with emails and phones")
    void testContactToResponseDtoConversion() {
        // Arrange
        when(contactRepository.findByIdAndUserId(contact.getId(), user.getId())).thenReturn(Optional.of(contact));

        // Act
        ContactResponseDto result = contactService.getContactById(user.getId(), contact.getId());

        // Assert
        assertNotNull(result);
        assertEquals(contact.getId(), result.getId());
        assertEquals(contact.getFirstName(), result.getFirstName());
        assertEquals(contact.getLastName(), result.getLastName());
        assertEquals(contact.getTitle(), result.getTitle());
        assertEquals(contact.getCreatedAt(), result.getCreatedAt());
        assertEquals(contact.getUpdatedAt(), result.getUpdatedAt());

        // Verify emails
        assertEquals(1, result.getEmails().size());
        EmailDto emailDto = result.getEmails().get(0);
        assertEquals(email.getId(), emailDto.getId());
        assertEquals(email.getEmail(), emailDto.getEmail());
        assertEquals(email.getLabel(), emailDto.getLabel());

        // Verify phones
        assertEquals(1, result.getPhones().size());
        PhoneDto phoneDto = result.getPhones().get(0);
        assertEquals(phone.getId(), phoneDto.getId());
        assertEquals(phone.getNumber(), phoneDto.getNumber());
        assertEquals(phone.getLabel(), phoneDto.getLabel());
    }

    @Test
    @DisplayName("Should handle contact with null emails and phones during DTO conversion")
    void testContactToResponseDtoWithNullCollections() {
        // Arrange
        Contact contactWithNullCollections = new Contact();
        contactWithNullCollections.setId(2L);
        contactWithNullCollections.setFirstName("John");
        contactWithNullCollections.setLastName("Doe");
        contactWithNullCollections.setUser(user);
        contactWithNullCollections.setEmails(null);
        contactWithNullCollections.setPhones(null);

        when(contactRepository.findByIdAndUserId(contactWithNullCollections.getId(), user.getId()))
                .thenReturn(Optional.of(contactWithNullCollections));

        // Act
        ContactResponseDto result = contactService.getContactById(user.getId(), contactWithNullCollections.getId());

        // Assert
        assertNotNull(result);
        assertNull(result.getEmails());
        assertNull(result.getPhones());
    }

    @Test
    @DisplayName("Should create contact with multiple emails and phones")
    void testCreateContactWithMultipleEmailsAndPhones() {
        // Arrange
        ContactRequestDto requestDto = new ContactRequestDto();
        requestDto.setFirstName("Jane");
        requestDto.setLastName("Smith");
        requestDto.setTitle("Developer");
        requestDto.setEmails(Arrays.asList(
                new EmailDto(null, "jane.work@example.com", "Work"),
                new EmailDto(null, "jane.personal@example.com", "Personal")
        ));
        requestDto.setPhones(Arrays.asList(
                new PhoneDto(null, "+1234567890", "Mobile"),
                new PhoneDto(null, "+0987654321", "Home")
        ));

        Contact multiContact = new Contact();
        multiContact.setId(1L);
        multiContact.setFirstName("Jane");
        multiContact.setLastName("Smith");
        multiContact.setTitle("Developer");
        multiContact.setUser(user);

        Email email1 = new Email(1L, "jane.work@example.com", "Work", multiContact);
        Email email2 = new Email(2L, "jane.personal@example.com", "Personal", multiContact);
        Phone phone1 = new Phone(1L, "+1234567890", "Mobile", multiContact);
        Phone phone2 = new Phone(2L, "+0987654321", "Home", multiContact);

        multiContact.setEmails(Arrays.asList(email1, email2));
        multiContact.setPhones(Arrays.asList(phone1, phone2));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(contactRepository.save(any(Contact.class))).thenReturn(multiContact);
        when(emailRepository.saveAll(anyList())).thenReturn(Arrays.asList(email1, email2));
        when(phoneRepository.saveAll(anyList())).thenReturn(Arrays.asList(phone1, phone2));

        // Act
        ContactResponseDto result = contactService.createContact(user.getId(), requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getEmails().size());
        assertEquals(2, result.getPhones().size());
        assertEquals("jane.work@example.com", result.getEmails().get(0).getEmail());
        assertEquals("jane.personal@example.com", result.getEmails().get(1).getEmail());
        assertEquals("+1234567890", result.getPhones().get(0).getNumber());
        assertEquals("+0987654321", result.getPhones().get(1).getNumber());

        // Verify
        verify(userRepository).findById(user.getId());
        verify(contactRepository).save(any(Contact.class));
        verify(emailRepository).saveAll(anyList());
        verify(phoneRepository).saveAll(anyList());
    }
}
