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

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> getAllContacts(String search, int page, int size, String sortBy, String sortDir, String userEmail) {
        User user = getUserByEmail(userEmail);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Contact> contactPage;

        if (search != null && !search.isBlank()) {
            log.info("Searching contacts for userId={} with term: {}", user.getId(), search);
            contactPage = contactRepository.searchByUserId(user.getId(), search.trim(), pageable);
        } else {
            log.info("Fetching all contacts for userId={}, page: {}, size: {}", user.getId(), page, size);
            contactPage = contactRepository.findByUserId(user.getId(), pageable);
        }

        return contactPage.map(this::mapToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponseDto getContactById(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Contact contact = findContactOwnedByUser(id, user.getId());
        log.info("Contact retrieved: id={} for userId={}", id, user.getId());
        return mapToResponseDto(contact);
    }

    @Override
    @Transactional
    public ContactResponseDto createContact(ContactRequestDto request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        contact.setUser(user);
        contact.setEmails(new ArrayList<>());
        contact.setPhones(new ArrayList<>());

        applyEmails(contact, request.getEmails());
        applyPhones(contact, request.getPhones());

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact created successfully: id={} for userId={}",
                savedContact.getId(), user.getId());
        return mapToResponseDto(savedContact);
    }

    @Override
    @Transactional
    public ContactResponseDto updateContact(Long id, ContactRequestDto request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Contact contact = findContactOwnedByUser(id, user.getId());

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());

        contact.getEmails().clear();
        applyEmails(contact, request.getEmails());

        contact.getPhones().clear();
        applyPhones(contact, request.getPhones());

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact updated successfully: id={} for userId={}", id, user.getId());
        return mapToResponseDto(savedContact);
    }

    @Override
    @Transactional
    public void deleteContact(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Contact contact = findContactOwnedByUser(id, user.getId());
        contactRepository.delete(contact);
        log.info("Contact deleted successfully: id={} for userId={}", id, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> getContactsByUser(Long userId, Pageable pageable) {
        return contactRepository.findByUserId(userId, pageable).map(this::mapToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> searchContacts(Long userId, String searchTerm, Pageable pageable) {
        return contactRepository.searchContacts(userId, searchTerm, pageable).map(this::mapToResponseDto);
    }

    @Override
    @Transactional
    public ContactResponseDto createContact(Long userId, ContactRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        contact.setUser(user);
        contact.setEmails(new ArrayList<>());
        contact.setPhones(new ArrayList<>());
        Contact savedContact = contactRepository.save(contact);
        savedContact.setEmails(new ArrayList<>());
        savedContact.setPhones(new ArrayList<>());
        applyEmails(savedContact, request.getEmails());
        applyPhones(savedContact, request.getPhones());
        if (!savedContact.getEmails().isEmpty()) savedContact.setEmails(emailRepository.saveAll(savedContact.getEmails()));
        if (!savedContact.getPhones().isEmpty()) savedContact.setPhones(phoneRepository.saveAll(savedContact.getPhones()));
        return mapToResponseDto(savedContact);
    }

    @Override
    @Transactional
    public ContactResponseDto updateContact(Long userId, Long contactId, ContactRequestDto request) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
            .orElseThrow(() -> new UnauthorizedAccessException("You do not have permission to update this contact"));
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        if (request.getEmails() != null) {
            List<Email> existingEmails = contact.getEmails();
            emailRepository.deleteAll(existingEmails);
            contact.setEmails(new ArrayList<>());
            applyEmails(contact, request.getEmails());
            contact.setEmails(emailRepository.saveAll(contact.getEmails()));
        }
        if (request.getPhones() != null) {
            List<Phone> existingPhones = contact.getPhones();
            phoneRepository.deleteAll(existingPhones);
            contact.setPhones(new ArrayList<>());
            applyPhones(contact, request.getPhones());
            contact.setPhones(phoneRepository.saveAll(contact.getPhones()));
        }
        return mapToResponseDto(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public void deleteContact(Long userId, Long contactId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
            .orElseThrow(() -> new UnauthorizedAccessException("You do not have permission to delete this contact"));
        contactRepository.delete(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponseDto getContactById(Long userId, Long contactId) {
        return mapToResponseDto(contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found")));
    }

    private void applyEmails(Contact contact, List<EmailDto> emailDtos) {
        if (emailDtos == null) {
            return;
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
        for (PhoneDto phoneDto : phoneDtos) {
            Phone phone = new Phone();
            phone.setNumber(phoneDto.getNumber());
            phone.setLabel(phoneDto.getLabel());
            phone.setContact(contact);
            contact.getPhones().add(phone);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.error("Authenticated user not found");
                    return new ResourceNotFoundException("User not found");
                });
    }

    private Contact findContactOwnedByUser(Long contactId, Long userId) {
        return contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> {
                    log.warn("Contact not found or access denied: id={}, userId={}", contactId, userId);
                    return new ResourceNotFoundException("Contact not found with id: " + contactId);
                });
    }

    private ContactResponseDto mapToResponseDto(Contact contact) {
        ContactResponseDto dto = new ContactResponseDto();
        dto.setId(contact.getId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setTitle(contact.getTitle());
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUpdatedAt(contact.getUpdatedAt());

        if (contact.getEmails() != null) {
            List<EmailDto> emailDtos = contact.getEmails().stream()
                    .map(email -> new EmailDto(email.getId(), email.getEmail(), email.getLabel()))
                    .collect(Collectors.toList());
            dto.setEmails(emailDtos);
        } else {
            dto.setEmails(null);
        }

        if (contact.getPhones() != null) {
            List<PhoneDto> phoneDtos = contact.getPhones().stream()
                    .map(phone -> new PhoneDto(phone.getId(), phone.getNumber(), phone.getLabel()))
                    .collect(Collectors.toList());
            dto.setPhones(phoneDtos);
        } else {
            dto.setPhones(null);
        }

        return dto;
    }
}

