package com.pdfmanagement.controller.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing the result of a PDF search operation.
 * <p>
 * This class encapsulates the essential information about a PDF file found during a search,
 * including its unique identifier, filename, uploader information, upload timestamp, and a URL
 * for accessing detailed information about the PDF.
 * </p>
 *
 * <ul>
 *   <li><b>id</b>: Unique identifier of the PDF file.</li>
 *   <li><b>filename</b>: Name of the PDF file.</li>
 *   <li><b>uploadedBy</b>: Username or identifier of the user who uploaded the PDF.</li>
 *   <li><b>uploadTime</b>: Date and time when the PDF was uploaded.</li>
 *   <li><b>detailsUrl</b>: URL to access more details about the PDF file.</li>
 * </ul>
 *
 * <p>
 * This DTO is typically used to transfer search result data between the backend and frontend layers.
 * </p>
 */
@Getter
@Setter 
public class PdfSearchResult {
    private Long id;
    private String filename;
    private String uploadedBy;
    private LocalDateTime uploadTime;
    private String detailsUrl;

    public PdfSearchResult(Long id, String filename, String uploadedBy, LocalDateTime uploadTime, String detailsUrl) {
        this.id = id;
        this.filename = filename;
        this.uploadedBy = uploadedBy;
        this.uploadTime = uploadTime;
        this.detailsUrl = detailsUrl;
    }
}
