# Contact Management System
## Cohort 9 Project by Badar Ali

A full-stack contact management application built with **Spring Boot** and **React.js**. Authenticated users can register, log in, manage their profile, and maintain their personal contacts.

## Features

### Authentication & Authorization

* User registration and login
* Login using email or phone number
* JWT-based authentication
* BCrypt password hashing
* Password change
* Protected routes and endpoints

### Contact Management

* Create, view, update, and delete contacts
* Paginated contact listing
* Search and filter contacts
* Sort contacts
* Multiple labeled email addresses
* Multiple labeled phone numbers

Each contact includes:

* First Name
* Last Name
* Title
* Email Addresses
* Phone Numbers

### User Profile

* View user information
* Change password
* Logout

### Exception Handling & Logging

* Global exception handling
* Meaningful error responses
* Application logging using **SLF4J and Logback**

### Testing

* JUnit
* Mockito
* H2 database for testing

### Code Quality

* SonarQube for code quality analysis
* Git for version control

## Technology Stack

### Backend

* Java 17
* Spring Boot 3.2.5
* Spring Web
* Spring Security
* Spring Data JPA
* Hibernate
* JJWT
* SQL Server
* H2
* JUnit
* Mockito
* SLF4J
* Logback

### Frontend

* React 18
* Vite
* React Router
* Axios
* Tailwind CSS
* Lucide React

## Project Structure

```text
backend/
└── src/main/java/com/example/backend/
    ├── controller/
    ├── dto/
    ├── entity/
    ├── exception/
    ├── repository/
    ├── security/
    └── service/

frontend/
└── src/
    ├── api/
    ├── components/
    ├── context/
    └── pages/
```

## Database

The application uses **Microsoft SQL Server** for development and **H2** for testing.

The main data includes:

* Users
* Contacts
* Contact Emails
* Contact Phones

## Running the Application

### Backend

```powershell
cd backend
./mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

## API Endpoints

### Authentication

| Method | Endpoint             | Description     |
| ------ | -------------------- | --------------- |
| POST   | `/api/auth/register` | Register a user |
| POST   | `/api/auth/login`    | Login           |

### Contacts

| Method | Endpoint             | Description    |
| ------ | -------------------- | -------------- |
| GET    | `/api/contacts`      | Get contacts   |
| GET    | `/api/contacts/{id}` | Get contact    |
| POST   | `/api/contacts`      | Create contact |
| PUT    | `/api/contacts/{id}` | Update contact |
| DELETE | `/api/contacts/{id}` | Delete contact |

### User

| Method | Endpoint              | Description     |
| ------ | --------------------- | --------------- |
| PUT    | `/api/users/password` | Change password |

Authenticated endpoints require:

```http
Authorization: Bearer <token>
```

## Application Screens

### Login & Registration

* Login form
* Registration form
* Authentication
* Redirect to dashboard after successful login

### Contact Management

* Paginated contact list
* Search/filter
* Create contact
* Update contact
* Delete contact
* Contact details

### User Profile

* User information
* Change password
* Logout

## Testing

Run backend tests with:

```powershell
cd backend
./mvnw.cmd test
```

## Build

### Backend

```powershell
cd backend
./mvnw.cmd clean package
```

### Frontend

```powershell
cd frontend
npm run build
```

## Security

* JWT bearer authentication
* BCrypt password hashing
* Stateless authentication
* Protected contact endpoints
* Users can access only their own contacts
* Configurable CORS
