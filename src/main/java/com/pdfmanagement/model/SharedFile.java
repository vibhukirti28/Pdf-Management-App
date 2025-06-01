package com.pdfmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a shared PDF file with a unique share token.
 * <p>
 * This class maps to a database table for tracking shared PDF files,
 * associating each shared file with a {@link PDFFile} entity and a unique share token.
 * The share token is generated automatically before persisting if not set.
 * The creation timestamp is also set automatically if not provided.
 * </p>
 *
 * Fields:
 * <ul>
 *   <li>id - Primary key identifier for the shared file.</li>
 *   <li>pdfFile - The associated PDF file being shared.</li>
 *   <li>shareToken - Unique token used for sharing and identifying the file.</li>
 *   <li>createdAt - Timestamp indicating when the share was created.</li>
 * </ul>
 *
 * Lifecycle:
 * <ul>
 *   <li>On persist, generates a unique share token and sets the creation timestamp if not already set.</li>
 * </ul>
 */
@Entity
@Getter
@Setter
public class SharedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pdf_id")
    private PDFFile pdfFile;

    @Column(unique = true)
    private String shareToken;

    private LocalDateTime createdAt;

    public SharedFile() {
    }

    @PrePersist
    public void generateShareToken() {
        if (this.shareToken == null) {
            this.shareToken = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

}
