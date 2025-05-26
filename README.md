# PDF Management & Collaboration System

A web application for managing and collaborating on PDF documents with user authentication, PDF sharing, and commenting features.

## Project Structure

- `/` (Root): Contains the Spring Boot backend application.
- `/frontend`: Contains the React + Vite frontend application.

## Features

- **User Management:**
  - User registration and authentication (JWT-based).
  - Secure password storage using BCrypt.
- **PDF Management:**
  - Upload PDF files (authenticated users).
  - View list of own uploaded PDF files.
  - Search own PDF files by filename.
  - Download own PDF files.
- **PDF Sharing:**
  - Generate unique, shareable links for PDFs (owner only).
  - Public access to PDF details via share token.
  - Public download of PDFs via share token.
- **Collaboration:**
  - Add comments to PDF files (authenticated users).
  - View comments on PDF files.
- **General:**
  - Public search for PDF files by filename.
  - RESTful API with protected endpoints.
  - CORS configuration for frontend integration.

## Prerequisites

- Java 21 or higher (or ensure compatibility with Spring Boot 3.1.0)
- Maven 3.6.3 or higher
- PostgreSQL 12 or higher
- Node.js 18 or higher (for frontend)
- npm 9 or higher (or yarn)

## Setup

### 1. Backend (Spring Boot)

   a. **Database Setup:**
      - Ensure PostgreSQL is running.
      - Create a new PostgreSQL database (e.g., `pdf_management`).
      - Update the database configuration in `src/main/resources/application.properties`:
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/pdf_management
        spring.datasource.username=your_postgres_username
        spring.datasource.password=your_postgres_password
        
        # Ensure Hibernate creates/updates the schema
        spring.jpa.hibernate.ddl-auto=update
        ```

   b. **JWT Configuration:**
      - **Important:** The JWT secret key is currently hardcoded in `src/main/java/com/pdfmanagement/util/JwtUtil.java`. For production, this **must** be externalized to `application.properties` or environment variables.
      - If externalized, you would add to `application.properties`:
        ```properties
        # jwt.secret=your_very_secure_secret_key_here 
        # jwt.expiration=86400 # 24 hours in seconds (default is 10 hours in JwtUtil.java)
        ```

   c. **Build the Application:**
      ```bash
      mvn clean install
      ```

   d. **Run the Application:**
      ```bash
      mvn spring-boot:run
      ```
      The backend will be available at `http://localhost:8081`.

### 2. Frontend (React + Vite)

   a. **Navigate to Frontend Directory:**
      ```bash
      cd frontend
      ```

   b. **Install Dependencies:**
      ```bash
      npm install
      # or if you use yarn: yarn install
      ```

   c. **Run the Frontend Development Server:**
      ```bash
      npm run dev
      # or if you use yarn: yarn dev
      ```
      The frontend will be available at `http://localhost:5173` (or another port if 5173 is busy).
      The Vite development server is configured to proxy API requests from `/api` to `http://localhost:8081` (see `frontend/vite.config.js`).

## API Endpoints

All endpoints are prefixed with `/api`.

### Authentication (`/auth`)
- `POST /register`: Register a new user. 
  - Body: `{ "username": "user", "email": "user@example.com", "password": "password123" }`
- `POST /login`: Authenticate and get JWT token.
  - Body: `{ "email": "user@example.com", "password": "password123" }`

### User (`/users` - Requires Authentication)
- `GET /profile`: Get current user's profile.

### PDF Management (`/pdf`)
- `POST /upload`: Upload a PDF file (Authenticated).
  - Form Data: `file` (the PDF file)
- `GET /my-files`: Get a list of PDFs uploaded by the authenticated user.
- `GET /my-files/search?q={query}`: Search own PDFs by filename (Authenticated).
- `GET /search?q={query}`: Search all PDFs by filename (Public).
- `GET /{id}`: Get PDF details and its comments by PDF ID (Public).
- `GET /download/{id}`: Download a PDF file by ID (Authenticated, owner only).
- `POST /{id}/share`: Generate a shareable link for a PDF (Authenticated, owner only).

### Shared PDFs (`/shared` - Public Access via Token)
- `GET /access/{shareToken}`: Access PDF details using a share token.
- `GET /download/{shareToken}`: Download a PDF using a share token.

### Comments (`/pdf/{pdfId}/comments` - Requires Authentication)
- `POST /`: Add a comment to a PDF specified by `pdfId`.
  - Body: `{ "text": "This is a comment." }`

## API Documentation

API documentation is available using Swagger UI (once the backend is running):
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8081/v3/api-docs`

## Frontend Integration

The backend API is configured via `SecurityConfig.java` to accept requests from the Vite frontend development server (default `http://localhost:5173`). If your frontend runs on a different port, update the CORS configuration in `com.pdfmanagement.config.SecurityConfig`.

## Security

- Passwords are hashed using BCrypt.
- JWT tokens are used for stateless authentication.
- CSRF protection is disabled (common for stateless REST APIs serving non-browser clients or SPAs with token auth).
- CORS is configured to allow requests from the specified frontend origin.

## Database

The application uses PostgreSQL. Database schema is currently managed by Hibernate's `ddl-auto` feature (set to `update` in `application.properties`), which automatically updates the schema based on entity definitions. For production, consider using a dedicated migration tool like Flyway or Liquibase for more control over schema changes.

## Testing

Run backend unit/integration tests using:
```bash
mvn test
```

## License

This project is licensed under the MIT License.
