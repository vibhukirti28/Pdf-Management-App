package com.pdfmanagement.repository;

import com.pdfmanagement.model.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link SharedFile} entities.
 * <p>
 * Extends {@link JpaRepository} to provide CRUD operations and custom queries for shared files.
 * </p>
 *
 * <p>
 * Provides a method to retrieve a {@link SharedFile} by its share token.
 * </p>
 *
 * @author YourName
 */
public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
    Optional<SharedFile> findByShareToken(String shareToken);
}
