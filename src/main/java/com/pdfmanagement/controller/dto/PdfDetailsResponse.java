package com.pdfmanagement.controller.dto;

import com.pdfmanagement.model.PDFFile;
import com.pdfmanagement.model.Comment;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing the details of a PDF file along with its associated comments.
 * <p>
 * This class encapsulates the response structure for PDF details, including the PDF file information
 * and a list of comments related to the PDF.
 * </p>
 *
 * @author YourName
 */
@Getter
@Setter 
public class PdfDetailsResponse {
    private PDFFileResponse pdfFile;
    private List<CommentResponse> comments;

    public PdfDetailsResponse(PDFFile pdfFile, List<Comment> comments) {
        this.pdfFile = new PDFFileResponse(pdfFile);
        this.comments = comments.stream().map(CommentResponse::new).collect(Collectors.toList());
    }
}
