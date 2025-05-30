package com.pdfmanagement.controller;

import com.pdfmanagement.model.PDFFile;
import com.pdfmanagement.model.Comment;
import com.pdfmanagement.controller.dto.PDFFileResponse;
import com.pdfmanagement.controller.dto.PdfSearchResult;
import com.pdfmanagement.controller.dto.CommentRequest;
import com.pdfmanagement.controller.dto.CommentResponse;
import com.pdfmanagement.controller.dto.PdfDetailsResponse;
import com.pdfmanagement.repository.CommentRepository;
import com.pdfmanagement.repository.PDFRepository;
import com.pdfmanagement.service.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus; // Added HttpStatus import

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pdf")
public class PDFController {

    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PDFRepository pdfRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private com.pdfmanagement.repository.SharedFileRepository sharedFileRepository; // Added SharedFileRepository

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            String email = authentication.getName(); // This is now the email
            String filePath = fileStorageService.storeFile(file);

            PDFFile pdfFile = new PDFFile();
            pdfFile.setFilename(file.getOriginalFilename());
            pdfFile.setFilepath(filePath);
            pdfFile.setUploadedBy(email);
            pdfFile.setUploadTime(LocalDateTime.now());

            pdfRepository.save(pdfFile);

            return ResponseEntity.ok("PDF uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // 1. Search endpoint returns detailsUrl to get PDF metadata + comments
    // @GetMapping("/search") // Removed duplicate/conflicting mapping

    @GetMapping("/my-files/search")
    public ResponseEntity<List<PDFFileResponse>> searchMyFiles(@RequestParam("q") String query,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String email = authentication.getName(); // This is now the email
        List<PDFFile> userPdfs = pdfRepository.findByUploadedByAndFilenameContainingIgnoreCase(email, query);
        return ResponseEntity.ok(userPdfs.stream().map(pdf -> new PDFFileResponse(pdf)).toList());
    }

    @GetMapping("/my-files")
    public ResponseEntity<List<PDFFileResponse>> getMyFiles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String email = authentication.getName(); // This is now the email
        List<PDFFile> userPdfs = pdfRepository.findByUploadedBy(email);
        return ResponseEntity.ok(userPdfs.stream().map(pdf -> new PDFFileResponse(pdf)).toList());
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
                    "/api/pdf/" + pdf.getId());
        }).toList();

        return ResponseEntity.ok(response);
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

    // 3. Download or display PDF file inline — accessible **only via shareable
    // link**
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadPdfForAuthenticatedUser(@PathVariable Long id,
            Authentication authentication) {
        // Initial check for authentication, though Spring Security should handle this
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthorized attempt to download PDF ID: {}. Authentication missing or invalid.", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUsername = authentication.getName();
        logger.info("Attempting to download PDF with ID: {} for user: {}", id, currentUsername);

        Optional<PDFFile> pdfFileOptional = pdfRepository.findById(id);

        if (!pdfFileOptional.isPresent()) {
            logger.warn("PDF file with ID: {} not found for user: {}.", id, currentUsername);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or .notFound().build();
        }

        PDFFile pdfFile = pdfFileOptional.get();
        logger.info("Found PDF: '{}' (ID: {}). Owner: {}. Requested by: {}", pdfFile.getFilename(), id,
                pdfFile.getUploadedBy(), currentUsername);

        // Ownership Check
        if (!pdfFile.getUploadedBy().equals(currentUsername)) {
            logger.warn("FORBIDDEN: User {} attempted to access PDF '{}' (ID: {}) owned by {}. Denying access.",
                    currentUsername, pdfFile.getFilename(), id, pdfFile.getUploadedBy());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        logger.info("Ownership check passed for PDF ID: {}. User: {}. Proceeding to serve file: {}", id,
                currentUsername, pdfFile.getFilepath());

        try {
            Path filePathObj = Paths.get(pdfFile.getFilepath()); // Renamed to avoid conflict if 'filePath' is used
                                                                 // elsewhere
            Resource resource = new UrlResource(filePathObj.toUri());

            if (resource.exists() && resource.isReadable()) {
                logger.info("Successfully loaded resource for PDF ID: {}", id);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfFile.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            }

            else {
                logger.error("Error: File not found or not readable at path: {} for PDF ID: {}", pdfFile.getFilepath(),
                        id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Internal server error
            }
        } catch (MalformedURLException e) {
            logger.error("Error: Malformed URL for filepath: {} for PDF ID: {}. Error: {}", pdfFile.getFilepath(), id,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Add comment to PDF
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody CommentRequest commentRequest,
            Authentication auth) {
        var pdfOpt = pdfRepository.findById(id);
        if (pdfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PDFFile pdfFile = pdfOpt.get();

        Comment comment = new Comment();
        comment.setPdfFile(pdfFile);
        comment.setUsername(auth.getName()); // authentication.getName() is email, store as username
        comment.setText(commentRequest.getText());
        comment.setCommentTime(LocalDateTime.now());
        commentRepository.save(comment);
        return ResponseEntity.ok(new CommentResponse(comment)); // Return the created comment
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<?> sharePdf(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        var pdfOpt = pdfRepository.findById(id);
        if (pdfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PDFFile pdfFile = pdfOpt.get();

        // Ensure the authenticated user is the owner of the PDF
        if (!pdfFile.getUploadedBy().equals(authentication.getName())) {
            return ResponseEntity.status(403).body("User not authorized to share this PDF");
        }

        // Create or find existing share record (optional: for now, always create new)
        com.pdfmanagement.model.SharedFile sharedFile = new com.pdfmanagement.model.SharedFile();
        sharedFile.setPdfFile(pdfFile);
        // The shareToken is auto-generated by the SharedFile entity's @PrePersist or
        // default value
        sharedFileRepository.save(sharedFile);

        // Construct the shareable link (adjust frontend URL as needed)
        String shareableLink = "http://localhost:5173/share/" + sharedFile.getShareToken(); // Updated port

        return ResponseEntity
                .ok(java.util.Map.of("shareableLink", shareableLink, "shareToken", sharedFile.getShareToken()));
    }

    @GetMapping("/shared/view/{shareToken}")
    public ResponseEntity<Resource> viewSharedPdf(@PathVariable String shareToken) {
        var sharedFileOpt = sharedFileRepository.findByShareToken(shareToken);
        if (sharedFileOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // Or a custom 'invalid link' page/response
        }
        com.pdfmanagement.model.SharedFile sharedFile = sharedFileOpt.get();
        PDFFile pdfFile = sharedFile.getPdfFile();

        if (pdfFile == null) {
            return ResponseEntity.notFound().build(); // Should not happen if data integrity is maintained
        }

        try {
            Path filePath = Paths.get(pdfFile.getFilepath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                // Log this error, as it indicates a missing file for a valid share token
                System.err.println(
                        "Error: File not found for shareToken: " + shareToken + ", filepath: " + pdfFile.getFilepath());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Internal server error
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfFile.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        }

        catch (MalformedURLException e) {
            System.err.println("Error: Malformed URL for filepath: " + pdfFile.getFilepath() + " for shareToken: "
                    + shareToken + ". Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
