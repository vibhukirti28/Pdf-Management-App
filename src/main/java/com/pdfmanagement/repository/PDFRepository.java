package com.pdfmanagement.repository;

import com.pdfmanagement.model.PDFFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link PDFFile} entities.
 * <p>
 * Extends {@link JpaRepository} to provide CRUD operations and custom query methods
 * for accessing PDF files stored in the database.
 * </p>
 *
 * <ul>
 *   <li>{@link #findByUploadedBy(String)}: Retrieves all PDF files uploaded by a specific user (by email).</li>
 *   <li>{@link #findByFilenameContainingIgnoreCase(String)}: Finds PDF files whose filenames contain the specified string, case-insensitive.</li>
 *   <li>{@link #findByUploadedByAndFilenameContainingIgnoreCase(String, String)}: Finds PDF files uploaded by a specific user and whose filenames contain the specified string, case-insensitive.</li>
 * </ul>
 */
public interface PDFRepository extends JpaRepository<PDFFile, Long> {
    List<PDFFile> findByUploadedBy(String email);

    List<PDFFile> findByFilenameContainingIgnoreCase(String filename);

    List<PDFFile> findByUploadedByAndFilenameContainingIgnoreCase(String uploadedBy, String filename);
}
