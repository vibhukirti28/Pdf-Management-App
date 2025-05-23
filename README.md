# PDF Management & Collaboration System

A web application for managing and collaborating on PDF documents with user authentication and authorization.

## Features

- User registration and authentication with JWT
- Secure password storage with BCrypt
- RESTful API for user management
- Protected endpoints with JWT authentication
- CORS configuration for frontend integration
- Database migration with Flyway

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- PostgreSQL 12 or higher

## Setup

1. **Database Setup**
   - Create a new PostgreSQL database named `pdf_management`
   - Update the database configuration in `src/main/resources/application.properties`:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/pdf_management
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

2. **JWT Configuration**
   - Update the JWT secret in `application.properties`:
     ```properties
     jwt.secret=your_very_secure_secret_key_here
     jwt.expiration=86400 # 24 hours in seconds
     ```

3. **Build the Application**
   ```bash
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```
   The application will be available at `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and get JWT token

### User Management
- `GET /api/users/profile` - Get current user's profile (requires authentication)

## API Documentation

API documentation is available using Swagger UI:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## Frontend Integration

The API is configured to accept requests from `http://localhost:3000` by default. Update the CORS configuration in `application.properties` if your frontend runs on a different port.

## Security

- Passwords are hashed using BCrypt
- JWT tokens are used for stateless authentication
- CSRF protection is disabled (as it's typically not needed for stateless REST APIs)
- CORS is configured to allow requests only from trusted origins

## Database

The application uses Flyway for database migrations. All schema changes should be added as new migration scripts in the `src/main/resources/db/migration` directory.

## Testing

Run the tests using:
```bash
mvn test
```

## License

This project is licensed under the MIT License.
