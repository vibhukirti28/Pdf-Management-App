package com.pdfmanagement.controller.dto;

import com.pdfmanagement.model.PDFFile;
import java.time.LocalDateTime;
    
import lombok.Getter;
import lombok.Setter;

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
