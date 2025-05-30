package com.pdfmanagement.controller;

import com.pdfmanagement.controller.dto.CreateUserRequest;
import com.pdfmanagement.model.User;
import com.pdfmanagement.repository.UserRepository;
import com.pdfmanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Added this import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Register endpoint
    /**
     * Endpoint to register a user.
     * @param createUserRequest a {@link CreateUserRequest} containing the user's email, username, and password
     * @return a ResponseEntity containing either a success message or an error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        // Check if email is already taken
        if (userRepository.findByEmail(createUserRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already taken");
        }

        // Create and save the user
        User user = new User();
        user.setDbUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));

        userRepository.save(user);

        // Return success message
        return ResponseEntity.ok("User registered successfully");
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginMap) {
        String email = loginMap.get("email");
        String password = loginMap.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        }

        catch (BadCredentialsException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid email or password");
            return ResponseEntity.status(401).body(errorResponse);
        }

        final String token = jwtUtil.generateToken(email);

        return ResponseEntity.ok(Map.of("jwtToken", token));
    }
}
