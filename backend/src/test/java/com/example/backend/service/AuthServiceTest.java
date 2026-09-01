package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private ChangePasswordRequest changePasswordRequest;
    private User user;

    @BeforeEach
    void setUp() {
        // Initialize test data
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPhone("+1234567890");
        registerRequest.setPassword("SecurePass123!");

        loginRequest = new LoginRequest();
        loginRequest.setIdentifier("john.doe@example.com");
        loginRequest.setPassword("SecurePass123!");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("SecurePass123!");
        changePasswordRequest.setNewPassword("NewPass456!");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhone("+1234567890");
        user.setPassword("encodedPassword");
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("Should successfully register a new user with BCrypt hashed password")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt.token.here");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@example.com", response.getEmail());

        // Verify
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).existsByPhone(registerRequest.getPhone());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken(user.getEmail());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void testRegisterFailureEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email is already registered", exception.getMessage());

        // Verify
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when phone number already exists")
    void testRegisterFailurePhoneExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Phone number is already registered", exception.getMessage());

        // Verify
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).existsByPhone(registerRequest.getPhone());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should successfully register when phone is blank")
    void testRegisterSuccessWithoutPhone() {
        // Arrange
        registerRequest.setPhone("");
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt.token.here");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());

        // Verify phone was never checked when blank
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository).save(any(User.class));
    }

    // ==================== Login Tests ====================

    @Test
    @DisplayName("Should successfully login with correct email and password")
    void testLoginSuccessByEmail() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(loginRequest.getIdentifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt.token.here");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@example.com", response.getEmail());

        // Verify
        verify(userRepository).findByEmailIgnoreCase(loginRequest.getIdentifier());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtTokenProvider).generateToken(user.getEmail());
    }

    @Test
    @DisplayName("Should successfully login with correct phone and password")
    void testLoginSuccessByPhone() {
        // Arrange
        loginRequest.setIdentifier("+1234567890");
        when(userRepository.findByEmailIgnoreCase(loginRequest.getIdentifier())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(loginRequest.getIdentifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt.token.here");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(1L, response.getId());

        // Verify
        verify(userRepository).findByEmailIgnoreCase(loginRequest.getIdentifier());
        verify(userRepository).findByPhone(loginRequest.getIdentifier());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user not found")
    void testLoginFailureUserNotFound() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(loginRequest.getIdentifier())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(loginRequest.getIdentifier())).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());

        // Verify
        verify(userRepository).findByEmailIgnoreCase(loginRequest.getIdentifier());
        verify(userRepository).findByPhone(loginRequest.getIdentifier());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when password is incorrect")
    void testLoginFailureIncorrectPassword() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(loginRequest.getIdentifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());

        // Verify
        verify(userRepository).findByEmailIgnoreCase(loginRequest.getIdentifier());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    // ==================== Change Password Tests ====================

    @Test
    @DisplayName("Should successfully change password when current password is correct")
    void testChangePasswordSuccess() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(changePasswordRequest.getNewPassword())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.changePassword(userId, changePasswordRequest);

        // Assert - Verify no exception is thrown

        // Verify
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(changePasswordRequest.getCurrentPassword(), "encodedPassword");
        verify(passwordEncoder).encode(changePasswordRequest.getNewPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when user not found during password change")
    void testChangePasswordUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.changePassword(userId, changePasswordRequest)
        );

        assertEquals("User not found", exception.getMessage());

        // Verify
        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when current password is incorrect")
    void testChangePasswordIncorrectCurrentPassword() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.changePassword(userId, changePasswordRequest)
        );

        assertEquals("Current password is incorrect", exception.getMessage());

        // Verify
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(changePasswordRequest.getCurrentPassword(), user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should verify password is BCrypt encoded during registration")
    void testPasswordEncodingDuringRegistration() {
        // Arrange
        String plainPassword = registerRequest.getPassword();
        String encodedPassword = "bcrypt_encoded_password_hash";

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt.token.here");

        // Act
        authService.register(registerRequest);

        // Assert & Verify - Ensure password encoder was called
        verify(passwordEncoder).encode(plainPassword);

        // Verify the user passed to save has the encoded password
        verify(userRepository).save(argThat(savedUser -> 
            savedUser.getPassword() != null && 
            savedUser.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    @DisplayName("Should verify password is BCrypt encoded during password change")
    void testPasswordEncodingDuringPasswordChange() {
        // Arrange
        Long userId = 1L;
        String newPlainPassword = changePasswordRequest.getNewPassword();
        String newEncodedPassword = "bcrypt_new_password_hash";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPlainPassword)).thenReturn(newEncodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.changePassword(userId, changePasswordRequest);

        // Assert & Verify
        verify(passwordEncoder).encode(newPlainPassword);
        
        // Verify the user passed to save has the new encoded password
        verify(userRepository).save(argThat(savedUser -> 
            savedUser.getPassword() != null && 
            savedUser.getPassword().equals(newEncodedPassword)
        ));
    }
}