package com.pdfmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a PDF file entity stored in the system.
 * <p>
 * This class contains metadata about the PDF file, including its filename,
 * storage path, uploader's username, and the upload timestamp.
 * </p>
 *
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code id} - Unique identifier for the PDF file (auto-generated).</li>
 *   <li>{@code filename} - Name of the PDF file.</li>
 *   <li>{@code filepath} - Path where the PDF file is stored on the server.</li>
 *   <li>{@code uploadedBy} - Username of the user who uploaded the file.</li>
 *   <li>{@code uploadTime} - Date and time when the file was uploaded.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This entity is mapped to a database table using JPA annotations.
 * </p>
 */
@Getter
@Setter

@Entity
public class PDFFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String filepath;
    private String uploadedBy; // Username
    private LocalDateTime uploadTime;

}
