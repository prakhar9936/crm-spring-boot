# CRM Backend – Spring Boot

A Customer Relationship Management (CRM) backend REST API built with Spring Boot, featuring JWT-based authentication and client management.

## Tech Stack
- Java 17, Spring Boot 3.5
- Spring Security + JWT (jjwt)
- Spring Data JPA + Hibernate
- H2 (in-memory, dev) / MySQL (production-ready)
- Maven

## Features
- User registration & login with BCrypt password hashing
- Stateless JWT authentication with custom filter
- Global exception handling with clean error responses
- Client management with employee ownership (assigned user)
- Role-ready architecture (Entity → Repository → DTO → Service → Controller)

## API Endpoints

### Auth
- `POST /api/auth/register` – Register a new user
- `POST /api/auth/login` – Login and receive a JWT

### Clients (JWT required)
- `POST /api/clients` – Create a client (auto-assigned to logged-in user)
- `GET /api/clients/my` – Get clients assigned to the logged-in user
- `GET /api/clients` – Get all clients

## Running Locally
```bash
mvn spring-boot:run
```
App runs on `http://localhost:9090`

## Status
🚧 In active development — Contract module and role-based access control coming next.