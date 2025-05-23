package com.pdfmanagement.repository;

import com.pdfmanagement.model.Comment;
import com.pdfmanagement.model.PDFFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPdfFile(PDFFile pdfFile);
}
