# CRM Backend - Spring Boot

A Customer Relationship Management (CRM) backend REST API built with Spring Boot. It has JWT login, two roles (ADMIN and EMPLOYEE), client management, contracts, and ownership rules.

## Tech Stack
- Java 17+ (tested on Java 24), Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA + Hibernate
- MySQL 8
- Swagger UI (springdoc)
- Lombok
- JUnit 5 + Mockito
- Maven (use the included mvnw.cmd)

## Features
- Register and login with BCrypt password hashing and JWT tokens
- Roles: ADMIN and EMPLOYEE (the first registered user becomes ADMIN, everyone after is EMPLOYEE)
- Client CRUD with pagination, search by name, and "my clients"
- Contract CRUD nested under clients
- Ownership rules (see below)
- Clean JSON error responses (400, 401, 403, 404, 409)
- Swagger UI with an Authorize button for the JWT token

## Roles and Ownership Rules
- Every client is assigned to the user who created it.
- ADMIN can change or delete any client and any contract.
- EMPLOYEE can change or delete only their own clients, and only contracts that belong to their own clients. Otherwise the API returns 403.
- Any logged-in user can read clients and contracts.
- Only ADMIN can list users, promote/demote, or activate/deactivate a user (an admin cannot demote or deactivate themselves).
- Inactive users cannot log in (403 "Account is inactive").
- A client that still has contracts cannot be deleted (409 Conflict).
- Wrong email or password returns 401.
- A new client or contract with bad data returns 400 with field messages (for contracts: status is required and end date must be after start date).

## API Endpoints
All endpoints except /api/auth/** and Swagger need a JWT token (Authorization: Bearer TOKEN).

### Auth
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive a JWT

### Users
- `GET /api/users/me` - Details of the logged-in user
- `PUT /api/users/{id}/promote` - Promote a user to ADMIN (ADMIN only)
- `PUT /api/users/{id}/demote` - Demote an ADMIN to EMPLOYEE (ADMIN only, cannot demote yourself)

### Users (ADMIN)
- `GET /api/users?page=0&size=10` - List all users (paginated, ADMIN)
- `PUT /api/users/{id}/activate` - Activate a user account (ADMIN)
- `PUT /api/users/{id}/deactivate` - Deactivate a user account (ADMIN, cannot deactivate self)

### Clients
- `POST /api/clients` - Create a client (assigned to the logged-in user)
- `GET /api/clients?page=0&size=10` - List all clients (paginated)
- `GET /api/clients/search?name=raj&page=0&size=10` - Search clients by name
- `GET /api/clients/my` - Clients assigned to the logged-in user
- `GET /api/clients/{id}` - Get one client
- `PUT /api/clients/{id}` - Update a client (owner or ADMIN)
- `DELETE /api/clients/{id}` - Delete a client (owner or ADMIN, 409 if it has contracts)

### Contracts
- `POST /api/clients/{clientId}/contracts` - Create a contract for a client (owner or ADMIN)
- `GET /api/contracts?status=ACTIVE&page=0&size=10` - List all contracts (paginated, optional status filter)
- `GET /api/clients/{clientId}/contracts` - List contracts of a client
- `PUT /api/contracts/{id}` - Update a contract (owner of the client or ADMIN)
- `DELETE /api/contracts/{id}` - Delete a contract (owner of the client or ADMIN)

### Dashboard
- `GET /api/dashboard` - Aggregated CRM statistics (clients, users, contracts by status, contract values)

Note: client ids and contract ids are different numbers.

## Run Locally (Windows PowerShell)

1. Install Java 17 or newer and MySQL 8. This project expects MySQL on 127.0.0.1 port 3307. If your MySQL uses another port, change it in `src/main/resources/application.properties`. The database `crm_db` is created automatically.

2. In every NEW PowerShell window, set the database password, then start the app:

```powershell
$env:DB_PASSWORD = "your_mysql_password"
.\mvnw.cmd spring-boot:run
```

3. The app runs on `http://localhost:9090`.

4. Open Swagger UI: `http://localhost:9090/swagger-ui/index.html`
   - Call `POST /api/auth/register`, then `POST /api/auth/login`.
   - Copy only the token, click the green Authorize button, paste it (without the word Bearer), and click Authorize.

## Environment Variables
- `DB_PASSWORD` - MySQL password for user root (required)
- `JWT_SECRET` - Secret key used to sign JWT tokens (use a long random value, at least 32 characters, in any real deployment)

## Run Tests
```powershell
.\mvnw.cmd clean test "-Dtest=AuthServiceTest,ClientServiceTest,ContractServiceTest,UserServiceTest,DashboardServiceTest"
```
There are 30 unit tests (Mockito).

## Project Structure
- `controller` - REST endpoints
- `service` - business logic and ownership checks
- `repository` - Spring Data JPA repositories
- `entity` - JPA entities (User, Client, Contract, Role, ContractStatus)
- `dto` - request and response objects
- `exception` - custom exceptions and GlobalExceptionHandler
- `config` - SecurityConfig, OpenApiConfig
- `security` - JwtAuthFilter, JwtUtil

## Status
Working: auth, roles, admin user management, clients, contracts, contract pagination and status filter, contract validation, dashboard statistics, inactive-user login block, demote endpoint, ownership rules, Swagger, unit tests.

Planned:
- More tests and Docker