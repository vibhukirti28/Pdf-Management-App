package com.pdfmanagement.controller;

import com.pdfmanagement.model.PDFFile;
import com.pdfmanagement.model.Comment;
import com.pdfmanagement.repository.CommentRepository;
import com.pdfmanagement.repository.PDFRepository;
import com.pdfmanagement.service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PDFController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PDFRepository pdfRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            String username = authentication.getName();
            String filePath = fileStorageService.storeFile(file);

            PDFFile pdfFile = new PDFFile();
            pdfFile.setFilename(file.getOriginalFilename());
            pdfFile.setFilepath(filePath);
            pdfFile.setUploadedBy(username);
            pdfFile.setUploadTime(LocalDateTime.now());

            pdfRepository.save(pdfFile);

            return ResponseEntity.ok("PDF uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // 1. Search endpoint returns detailsUrl to get PDF metadata + comments
    @GetMapping("/search")
    public ResponseEntity<?> searchPdfs(@RequestParam("q") String query) {
        var results = pdfRepository.findByFilenameContainingIgnoreCase(query);

        var response = results.stream().map(pdf -> {
            return new PdfSearchResult(
                    pdf.getId(),
                    pdf.getFilename(),
                    pdf.getUploadedBy(),
                    pdf.getUploadTime(),
                    "/api/pdf/" + pdf.getId()   // clickable details URL
            );
        }).toList();

        return ResponseEntity.ok(response);
    }

    public static class PdfSearchResult {
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

        public Long getId() { return id; }
        public String getFilename() { return filename; }
        public String getUploadedBy() { return uploadedBy; }
        public LocalDateTime getUploadTime() { return uploadTime; }
        public String getDetailsUrl() { return detailsUrl; }
    }

    // 2. Get PDF metadata + all comments by PDF id
    @GetMapping("/{id}")
    public ResponseEntity<?> getPdfDetails(@PathVariable Long id) {
        var pdfOpt = pdfRepository.findById(id);
        if (pdfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PDFFile pdfFile = pdfOpt.get();
        var comments = commentRepository.findByPdfFile(pdfFile);

        return ResponseEntity.ok(new PdfDetailsResponse(pdfFile, comments));
    }

    public static class PdfDetailsResponse {
        private PDFFile pdfFile;
        private List<Comment> comments;

        public PdfDetailsResponse(PDFFile pdfFile, List<Comment> comments) {
            this.pdfFile = pdfFile;
            this.comments = comments;
        }

        public PDFFile getPdfFile() { return pdfFile; }
        public List<Comment> getComments() { return comments; }
    }

    // 3. Download or display PDF file inline — accessible **only via shareable link**
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer) {
        // Allow download only if Referer header matches your domain (or is not blank and valid)
        // Adjust "yourdomain.com" to your actual frontend/backend domain
        if (referer == null || !referer.contains("yourdomain.com")) {
            return ResponseEntity.status(403).body(null); // Forbidden if direct typing without proper referer
        }

        var pdfOpt = pdfRepository.findById(id);
        if (pdfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PDFFile pdfFile = pdfOpt.get();

        try {
            Path filePath = Paths.get(pdfFile.getFilepath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfFile.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Add comment to PDF
    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody CommentRequest commentRequest, Authentication auth) {
        var pdfOpt = pdfRepository.findById(id);
        if (pdfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PDFFile pdfFile = pdfOpt.get();

        Comment comment = new Comment();
        comment.setPdfFile(pdfFile);
        comment.setUsername(auth.getName());
        comment.setText(commentRequest.getText());
        comment.setCommentTime(LocalDateTime.now());

        commentRepository.save(comment);
        return ResponseEntity.ok("Comment added");
    }

    public static class CommentRequest {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
