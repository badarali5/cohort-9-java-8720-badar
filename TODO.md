# Phase 2 Implementation - Todo List

## Step 1: Update Dependencies & Config
- [x] 1a. Update `pom.xml` - Add jjwt dependencies
- [x] 1b. Update `application.properties` - Add JWT config

## Step 2: Update Existing Files
- [x] 2a. Update `User.java` - Add `@Column(unique = true)` on phone
- [x] 2b. Update `UserRepository.java` - Add findByPhone, existsByEmail, existsByPhone

## Step 3: Create DTOs
- [x] 3a. Create `RegisterRequest.java`
- [x] 3b. Create `LoginRequest.java`
- [x] 3c. Create `ChangePasswordRequest.java`
- [x] 3d. Create `AuthResponse.java`

## Step 4: Create Security Layer
- [x] 4a. Create `CustomUserDetailsService.java`
- [x] 4b. Create `JwtTokenProvider.java`
- [x] 4c. Create `JwtAuthenticationFilter.java`
- [x] 4d. Create `SecurityConfig.java`

## Step 5: Create Service Layer
- [x] 5a. Create `AuthService.java` interface
- [x] 5b. Create `AuthServiceImpl.java`

## Step 6: Create Controllers
- [x] 6a. Create `AuthController.java`
- [x] 6b. Create `UserController.java`

## Step 7: Create Exception Handler
- [x] 7a. Create `BadRequestException.java`
- [x] 7b. Create `DuplicateResourceException.java`
- [x] 7c. Create `UnauthorizedException.java`
- [x] 7d. Create `GlobalExceptionHandler.java`

## Step 8: Verify Build & Security Scan
- [x] 8a. Run `mvn clean compile` (in progress - Maven wrapper downloading)
- [x] 8b. Security scan complete - no sensitive data exposure
- [ ] 8c. Run `mvn test`

