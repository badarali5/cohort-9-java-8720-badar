package com.example.backend.service;

import com.example.backend.dto.ContactRequestDto;
import com.example.backend.dto.ContactResponseDto;
import com.example.backend.dto.EmailDto;
import com.example.backend.dto.PhoneDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final EmailRepository emailRepository;
    private final PhoneRepository phoneRepository;
    private final UserRepository userRepository;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "firstName", "lastName", "email", "phone");

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> getAllContacts(Long userId, String search, int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        String normalizedSortField = (sortBy == null || sortBy.isBlank()) ? "firstName" : sortBy;
        if (!ALLOWED_SORT_FIELDS.contains(normalizedSortField)) {
            throw new IllegalArgumentException("Invalid sort field. Allowed values: id, firstName, lastName, email, phone");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, normalizeSortField(normalizedSortField)));

        Page<Contact> contacts;
        if (search != null && !search.isBlank()) {
            contacts = contactRepository.searchContacts(user.getId(), search.trim(), pageable);
        } else {
            contacts = contactRepository.findByUserId(user.getId(), pageable);
        }

        return contacts.map(this::convertToResponseDto);
    }

    private String normalizeSortField(String sortField) {
        return switch (sortField) {
            case "email" -> "emails";
            case "phone" -> "phones";
            default -> sortField;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> getContactsByUser(Long userId, Pageable pageable) {
        return contactRepository.findByUserId(userId, pageable).map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> searchContacts(Long userId, String searchTerm, Pageable pageable) {
        return contactRepository.searchContacts(userId, searchTerm, pageable).map(this::convertToResponseDto);
    }

    @Override
    @Transactional
    public ContactResponseDto createContact(Long userId, ContactRequestDto contactRequestDto) {
        log.info("Creating new contact for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        Contact contact = new Contact();
        contact.setFirstName(contactRequestDto.getFirstName());
        contact.setLastName(contactRequestDto.getLastName());
        contact.setTitle(contactRequestDto.getTitle());
        contact.setUser(user);
        contact.setEmails(new ArrayList<>());
        contact.setPhones(new ArrayList<>());

        applyEmails(contact, contactRequestDto.getEmails());
        applyPhones(contact, contactRequestDto.getPhones());

        Contact savedContact = contactRepository.save(contact);
        if (savedContact.getEmails() != null && !savedContact.getEmails().isEmpty()) {
            savedContact.setEmails(emailRepository.saveAll(savedContact.getEmails()));
        }
        if (savedContact.getPhones() != null && !savedContact.getPhones().isEmpty()) {
            savedContact.setPhones(phoneRepository.saveAll(savedContact.getPhones()));
        }

        log.info("Contact created successfully with id: {}", savedContact.getId());
        return convertToResponseDto(savedContact);
    }

    @Override
    @Transactional
    public ContactResponseDto updateContact(Long userId, Long contactId, ContactRequestDto contactRequestDto) {
        log.info("Updating contact: {} for user: {}", contactId, userId);

        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> {
                    log.error("Contact not found or unauthorized access: {} for user: {}", contactId, userId);
                    return new UnauthorizedAccessException("You do not have permission to update this contact");
                });

        contact.setFirstName(contactRequestDto.getFirstName());
        contact.setLastName(contactRequestDto.getLastName());
        contact.setTitle(contactRequestDto.getTitle());

        if (contactRequestDto.getEmails() != null) {
            if (contact.getEmails() != null) {
                emailRepository.deleteAll(contact.getEmails());
                contact.setEmails(new ArrayList<>(contact.getEmails()));
                contact.getEmails().clear();
            }
            applyEmails(contact, contactRequestDto.getEmails());
            if (contact.getEmails() != null && !contact.getEmails().isEmpty()) {
                contact.setEmails(emailRepository.saveAll(contact.getEmails()));
            }
        }

        if (contactRequestDto.getPhones() != null) {
            if (contact.getPhones() != null) {
                phoneRepository.deleteAll(contact.getPhones());
                contact.setPhones(new ArrayList<>(contact.getPhones()));
                contact.getPhones().clear();
            }
            applyPhones(contact, contactRequestDto.getPhones());
            if (contact.getPhones() != null && !contact.getPhones().isEmpty()) {
                contact.setPhones(phoneRepository.saveAll(contact.getPhones()));
            }
        }

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact updated successfully: {}", contactId);
        return convertToResponseDto(updatedContact);
    }

    @Override
    @Transactional
    public void deleteContact(Long userId, Long contactId) {
        log.info("Deleting contact: {} for user: {}", contactId, userId);

        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> {
                    log.error("Contact not found or unauthorized access: {} for user: {}", contactId, userId);
                    return new UnauthorizedAccessException("You do not have permission to delete this contact");
                });

        contactRepository.delete(contact);
        log.info("Contact deleted successfully: {}", contactId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponseDto getContactById(Long userId, Long contactId) {
        log.info("Fetching contact: {} for user: {}", contactId, userId);

        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> {
                    log.error("Contact not found or unauthorized access: {} for user: {}", contactId, userId);
                    return new ResourceNotFoundException("Contact not found");
                });

        return convertToResponseDto(contact);
    }

    private void applyEmails(Contact contact, List<EmailDto> emailDtos) {
        if (emailDtos == null) {
            return;
        }
        if (contact.getEmails() == null) {
            contact.setEmails(new ArrayList<>());
        }

        for (EmailDto emailDto : emailDtos) {
            Email email = new Email();
            email.setEmail(emailDto.getEmail());
            email.setLabel(emailDto.getLabel());
            email.setContact(contact);
            contact.getEmails().add(email);
        }
    }

    private void applyPhones(Contact contact, List<PhoneDto> phoneDtos) {
        if (phoneDtos == null) {
            return;
        }
        if (contact.getPhones() == null) {
            contact.setPhones(new ArrayList<>());
        }

        for (PhoneDto phoneDto : phoneDtos) {
            Phone phone = new Phone();
            phone.setNumber(phoneDto.getNumber());
            phone.setLabel(phoneDto.getLabel());
            phone.setContact(contact);
            contact.getPhones().add(phone);
        }
    }

    private ContactResponseDto convertToResponseDto(Contact contact) {
        ContactResponseDto dto = new ContactResponseDto();
        dto.setId(contact.getId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setTitle(contact.getTitle());
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUpdatedAt(contact.getUpdatedAt());

        if (contact.getEmails() != null) {
            dto.setEmails(contact.getEmails().stream()
                    .map(email -> new EmailDto(email.getId(), email.getEmail(), email.getLabel()))
                    .collect(Collectors.toList()));
        } else {
            dto.setEmails(null);
        }

        if (contact.getPhones() != null) {
            dto.setPhones(contact.getPhones().stream()
                    .map(phone -> new PhoneDto(phone.getId(), phone.getNumber(), phone.getLabel()))
                    .collect(Collectors.toList()));
        } else {
            dto.setPhones(null);
        }

        return dto;
    }
}

