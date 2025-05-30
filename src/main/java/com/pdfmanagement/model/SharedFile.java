package com.pdfmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
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

    // Getters and setters
    public Long getId() {
        return id;
    }

    public PDFFile getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(PDFFile pdfFile) {
        this.pdfFile = pdfFile;
    }

    public String getShareToken() {
        return shareToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
