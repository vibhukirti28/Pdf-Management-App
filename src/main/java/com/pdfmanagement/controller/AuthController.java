package com.pdfmanagement.controller;

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
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> userMap) {
        String username = userMap.get("username");
        String email = userMap.get("email");
        String password = userMap.get("password");

        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Username, email, and password are required");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already taken");
        }

        User user = new User();
        user.setDbUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginMap) {
        String email = loginMap.get("email");
        String password = loginMap.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
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
