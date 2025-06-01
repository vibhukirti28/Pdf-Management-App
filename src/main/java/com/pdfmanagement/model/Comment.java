package com.pdfmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a comment made by a user on a PDF file.
 * <p>
 * Each comment is associated with a specific PDF file and contains information
 * about the user who made the comment, the comment text, and the time the comment was made.
 * </p>
 *
 * Fields:
 * <ul>
 *   <li>id - Unique identifier for the comment.</li>
 *   <li>pdfFile - The PDF file to which this comment belongs.</li>
 *   <li>username - The name of the user who made the comment.</li>
 *   <li>text - The content of the comment.</li>
 *   <li>commentTime - The date and time when the comment was created.</li>
 * </ul>
 */
@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pdf_id")
    private PDFFile pdfFile;

    private String username;
    private String text;
    private LocalDateTime commentTime;

}
