package com.pdfmanagement.service;

import com.pdfmanagement.model.User;
import com.pdfmanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service implementation for loading user-specific data.
 * <p>
 * This class implements the {@link org.springframework.security.core.userdetails.UserDetailsService}
 * interface to provide custom logic for retrieving user details from the database
 * using the {@link UserRepository}. It is used by Spring Security for authentication.
 * </p>
 *
 * <p>
 * The {@code loadUserByUsername} method fetches a {@link User} entity by email,
 * and if found, returns a Spring Security {@link org.springframework.security.core.userdetails.User}
 * object with the user's credentials and authorities.
 * </p>
 *
 * @author [Your Name]
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see UserRepository
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}
