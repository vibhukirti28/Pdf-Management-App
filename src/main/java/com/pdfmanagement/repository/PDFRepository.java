package com.pdfmanagement.repository;

import com.pdfmanagement.model.PDFFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PDFRepository extends JpaRepository<PDFFile, Long> {
    List<PDFFile> findByUploadedBy(String username);
    List<PDFFile> findByFilenameContainingIgnoreCase(String filename);
}

