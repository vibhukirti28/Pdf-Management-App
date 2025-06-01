package com.pdfmanagement.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a user entity in the PDF Management application.
 * <p>
 * This class is mapped to the "users" table in the database and implements
 * the {@link org.springframework.security.core.userdetails.UserDetails} interface
 * for integration with Spring Security.
 * </p>
 *
 * <p>
 * The {@code User} entity contains basic user information such as username,
 * email, and password. The email is used as the principal identifier for authentication.
 * </p>
 *
 * <ul>
 *   <li>{@code id} - The unique identifier for the user (primary key).</li>
 *   <li>{@code username} - The user's display name (stored in the database).</li>
 *   <li>{@code email} - The user's email address (used for authentication).</li>
 *   <li>{@code password} - The user's hashed password.</li>
 * </ul>
 *
 * <p>
 * By default, this implementation does not assign any roles or authorities to the user.
 * All account status checks (expired, locked, credentials expired, enabled) return {@code true}.
 * </p>
 *
 * <p>
 * Note: The {@code getUsername()} method returns the user's email address to comply with
 * the {@code UserDetails} contract, while {@code getDbUsername()} provides access to the
 * database-stored username field.
 * </p>
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // no roles/authorities for now
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() { // This now returns the email as per UserDetails contract
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Getter and Setter for the database 'username' field
    public String getDbUsername() {
        return username;
    }

    public void setDbUsername(String username) {
        this.username = username;
    }

    // Getters and Setters for id, email, password

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
