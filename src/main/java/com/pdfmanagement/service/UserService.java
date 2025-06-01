package com.pdfmanagement.service;

import com.pdfmanagement.model.User;
import com.pdfmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user-related operations such as registration.
 * <p>
 * This service provides methods to register new users, ensuring that email addresses are unique
 * and that passwords are securely encoded before being persisted.
 * </p>
 *
 * <p>
 * Dependencies:
 * <ul>
 *   <li>{@link UserRepository} - for user data persistence and lookup</li>
 *   <li>{@link PasswordEncoder} - for encoding user passwords</li>
 * </ul>
 * </p>
 *
 * @author YourName
 * @since 1.0
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) { 
            throw new RuntimeException("Email is already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
