package com.pdfmanagement.controller.dto;

import com.pdfmanagement.model.PDFFile;
import com.pdfmanagement.model.Comment;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

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
