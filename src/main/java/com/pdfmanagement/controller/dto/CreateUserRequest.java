package com.pdfmanagement.controller.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for creating a new user.
 * <p>
 * This class encapsulates the necessary information required to register a new user,
 * including username, email, and password. Validation annotations are used to ensure
 * that the provided data meets the required constraints.
 * </p>
 *
 * <ul>
 *   <li><b>username</b>: Must not be blank and must be between 3 and 50 characters.</li>
 *   <li><b>email</b>: Must not be blank and must be a valid email address.</li>
 *   <li><b>password</b>: Must not be blank and must be between 8 and 50 characters.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * CreateUserRequest request = new CreateUserRequest();
 * request.setUsername("john_doe");
 * request.setEmail("john@example.com");
 * request.setPassword("securePassword123");
 * </pre>
 */
@Getter
@Setter
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;
}
