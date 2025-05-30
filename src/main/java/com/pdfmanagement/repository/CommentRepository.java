package com.pdfmanagement.repository;

import com.pdfmanagement.model.Comment;
import com.pdfmanagement.model.PDFFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * Retrieves a list of comments associated with a given PDF file.
     *
     * @param pdfFile the PDF file to search for comments, not null
     * @return a list of comments, empty if no comments were found
     */
    List<Comment> findByPdfFile(PDFFile pdfFile);
}
