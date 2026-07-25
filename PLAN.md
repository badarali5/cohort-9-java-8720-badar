# Phase 2 Implementation Plan: Authentication, Password Management & Logging

## Information Gathered

### Existing Codebase:
- **Spring Boot 4.1.0** (Spring Boot 3.x style) with Java 17
- **Entities**: `User`, `Contact`, `Email`, `Phone` — with JPA, Lombok, Validation annotations
- **Repositories**: `UserRepository` (has `findByEmail`), `ContactRepository`, `EmailRepository`, `PhoneRepository`
- **Dependencies already present**: `spring-boot-starter-security`, `spring-boot-starter-validation`, `spring-boot-starter-webmvc`, `lombok`, `mssql-jdbc`
- **Missing dependencies**: JWT (jjwt), Spring Security configuration
- **User entity**: Already has `email` (unique, required), `phone`, `password` fields with timestamps

### Key Observations:
1. `User.email` has `@NotBlank` and `unique=true` constraint — registration will require email (plus optional phone)
2. `UserRepository` only has `findByEmail` — needs `findByPhone`, `existsByEmail`, `existsByPhone` for checks
3. No `phone` column in `User` has unique constraint — should add for dedup checks
4. No JWT dependency in `pom.xml` — needs `jjwt-api`, `jjwt-impl`, `jjwt-jackson`

---

## Plan

### Step 1: Update `pom.xml`
- Add `io.jsonwebtoken` jjwt dependencies (api, impl, jackson) for JWT support

### Step 2: Update `User.java` Entity
- Add `@Column(unique = true)` on `phone` field to prevent duplicate phone registrations
- No other entity changes needed — email already has unique constraint

### Step 3: Update `UserRepository.java`
- Add `findByPhone(String phone)` — for login via phone
- Add `existsByEmail(String email)` — for duplicate email check
- Add `existsByPhone(String phone)` — for duplicate phone check

### Step 4: Update `application.properties`
- Add JWT configuration: secret key, expiration time

### Step 5: Create DTOs (`dto/` package)
- `RegisterRequest.java` — firstName, lastName, email, phone, password with validation
- `LoginRequest.java` — identifier (email or phone), password
- `ChangePasswordRequest.java` — currentPassword, newPassword
- `AuthResponse.java` — token, user details (id, firstName, lastName, email)

### Step 6: Create Security Layer (`security/` package)
- `CustomUserDetailsService.java` — implements `UserDetailsService`, loads user by email or phone
- `JwtTokenProvider.java` — generates, validates, parses JWT tokens
- `JwtAuthenticationFilter.java` — once-per-request filter extracting Bearer token
- `SecurityConfig.java` — security filter chain, password encoder bean, CORS config, endpoint permissions

### Step 7: Create Service Layer (`service/` package)
- `AuthService.java` — interface
- `AuthServiceImpl.java` — implements register, login, changePassword with SLF4J logging

### Step 8: Create Controllers (`controller/` package)
- `AuthController.java` — `POST /api/auth/register`, `POST /api/auth/login`
- `UserController.java` — `PUT /api/users/password` (authenticated)

### Step 9: Create Exception Handling (`exception/` package)
- `GlobalExceptionHandler.java` — `@RestControllerAdvice`, handles validation errors, duplicate resources, bad credentials, unauthorized access with SLF4J logging

### Step 10: Security Scan
- Review all generated code for vulnerabilities (plain-text passwords, JWT exposure, etc.)

---

## Dependent Files to Edit (existing files)
1. `backend/pom.xml` — add jjwt dependencies
2. `backend/src/main/java/com/example/backend/entity/User.java` — add unique on phone
3. `backend/src/main/java/com/example/backend/repository/UserRepository.java` — add query methods
4. `backend/src/main/resources/application.properties` — add JWT config

## New Files to Create
1. `backend/src/main/java/com/example/backend/dto/RegisterRequest.java`
2. `backend/src/main/java/com/example/backend/dto/LoginRequest.java`
3. `backend/src/main/java/com/example/backend/dto/ChangePasswordRequest.java`
4. `backend/src/main/java/com/example/backend/dto/AuthResponse.java`
5. `backend/src/main/java/com/example/backend/security/CustomUserDetailsService.java`
6. `backend/src/main/java/com/example/backend/security/JwtTokenProvider.java`
7. `backend/src/main/java/com/example/backend/security/JwtAuthenticationFilter.java`
8. `backend/src/main/java/com/example/backend/security/SecurityConfig.java`
9. `backend/src/main/java/com/example/backend/service/AuthService.java`
10. `backend/src/main/java/com/example/backend/service/AuthServiceImpl.java`
11. `backend/src/main/java/com/example/backend/controller/AuthController.java`
12. `backend/src/main/java/com/example/backend/controller/UserController.java`
13. `backend/src/main/java/com/example/backend/exception/GlobalExceptionHandler.java`

## Follow-up Steps
1. Run `mvn clean compile` to verify build
2. Run `mvn test` to verify no regressions

## Confirmation
Does this plan look good? Shall I proceed with the implementation?

