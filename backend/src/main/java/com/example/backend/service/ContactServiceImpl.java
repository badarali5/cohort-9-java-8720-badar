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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> getContactsByUser(Long userId, Pageable pageable) {
        log.info("Fetching paginated contacts for user: {}", userId);
        
        Page<Contact> contacts = contactRepository.findByUserId(userId, pageable);
        return contacts.map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> searchContacts(Long userId, String searchTerm, Pageable pageable) {
        log.info("Searching contacts for user: {} with term: {}", userId, searchTerm);
        
        Page<Contact> contacts = contactRepository.searchContacts(userId, searchTerm, pageable);
        return contacts.map(this::convertToResponseDto);
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

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact created successfully with id: {}", savedContact.getId());

        // Add emails if provided
        if (contactRequestDto.getEmails() != null && !contactRequestDto.getEmails().isEmpty()) {
            List<Email> emails = contactRequestDto.getEmails().stream()
                    .map(emailDto -> {
                        Email email = new Email();
                        email.setEmail(emailDto.getEmail());
                        email.setLabel(emailDto.getLabel());
                        email.setContact(savedContact);
                        return email;
                    })
                    .collect(Collectors.toList());
            emailRepository.saveAll(emails);
            savedContact.setEmails(emails);
        }

        if (contactRequestDto.getPhones() != null && !contactRequestDto.getPhones().isEmpty()) {
            List<Phone> phones = contactRequestDto.getPhones().stream()
                    .map(phoneDto -> {
                        Phone phone = new Phone();
                        phone.setNumber(phoneDto.getNumber());
                        phone.setLabel(phoneDto.getLabel());
                        phone.setContact(savedContact);
                        return phone;
                    })
                    .collect(Collectors.toList());
            phoneRepository.saveAll(phones);
            savedContact.setPhones(phones);
        }

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
            emailRepository.deleteAll(contact.getEmails());
            List<Email> emails = contactRequestDto.getEmails().stream()
                    .map(emailDto -> {
                        Email email = new Email();
                        email.setEmail(emailDto.getEmail());
                        email.setLabel(emailDto.getLabel());
                        email.setContact(contact);
                        return email;
                    })
                    .collect(Collectors.toList());
            contact.setEmails(emailRepository.saveAll(emails));
        }

        if (contactRequestDto.getPhones() != null) {
            phoneRepository.deleteAll(contact.getPhones());
            List<Phone> phones = contactRequestDto.getPhones().stream()
                    .map(phoneDto -> {
                        Phone phone = new Phone();
                        phone.setNumber(phoneDto.getNumber());
                        phone.setLabel(phoneDto.getLabel());
                        phone.setContact(contact);
                        return phone;
                    })
                    .collect(Collectors.toList());
            contact.setPhones(phoneRepository.saveAll(phones));
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
        }

        if (contact.getPhones() != null) {
            dto.setPhones(contact.getPhones().stream()
                    .map(phone -> new PhoneDto(phone.getId(), phone.getNumber(), phone.getLabel()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
