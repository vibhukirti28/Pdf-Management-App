package com.pdfmanagement.controller.dto;

import com.pdfmanagement.model.PDFFile;
import java.time.LocalDateTime;
    
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for representing a PDF file response.
 * <p>
 * This class encapsulates the details of a PDF file, including its ID, filename,
 * the user who uploaded it, and the upload timestamp. It is typically used to
 * transfer PDF file data between the backend and frontend layers of the application.
 * </p>
 *
 * @author YourName
 */
@Getter
@Setter
public class PDFFileResponse {
    private Long id;
    private String filename;
    private String uploadedBy;
    private LocalDateTime uploadTime;

    public PDFFileResponse(PDFFile pdfFile) {
        this.id = pdfFile.getId();
        this.filename = pdfFile.getFilename();
        this.uploadedBy = pdfFile.getUploadedBy();
        this.uploadTime = pdfFile.getUploadTime();
    }
}
