package com.pdfmanagement.controller.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

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
