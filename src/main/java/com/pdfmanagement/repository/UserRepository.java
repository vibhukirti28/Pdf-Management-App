package com.pdfmanagement.repository;

import com.pdfmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * <p>
 * Extends {@link JpaRepository} to provide CRUD operations and custom queries for User entities.
 * </p>
 *
 * <ul>
 *   <li>{@code findByEmail(String email)}: Retrieves an Optional containing the User with the specified email, if present.</li>
 *   <li>{@code existsByEmail(String email)}: Checks if a User with the specified email exists in the database.</li>
 * </ul>
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
