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

    /**
     * Handles the uploading of a PDF file.
     * <p>
     * This endpoint accepts a multipart file upload, stores the file using the fileStorageService,
     * and saves metadata about the uploaded PDF (such as filename, file path, uploader's email, and upload time)
     * to the database via the pdfRepository.
     * </p>
     *
     * @param file           the PDF file to be uploaded, received as a multipart file
     * @param authentication the authentication object containing the user's details (email)
     * @return a ResponseEntity indicating the result of the upload operation;
     *         returns a success message if the upload is successful, or an error message if it fails
     */
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

    /**
     * Searches for PDF files uploaded by the authenticated user that match the given query in their filename.
     *
     * @param query the search query to filter filenames (case-insensitive)
     * @param authentication the authentication object containing the user's credentials
     * @return a ResponseEntity containing a list of PDFFileResponse objects matching the search criteria,
     *         or a 401 Unauthorized status if the user is not authenticated
     */
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

    /**
     * Retrieves the list of PDF files uploaded by the currently authenticated user.
     *
     * <p>This endpoint requires the user to be authenticated. If the authentication is missing or invalid,
     * a 401 Unauthorized response is returned. Otherwise, it fetches all PDF files associated with the
     * authenticated user's email and returns them as a list of {@link PDFFileResponse} objects.</p>
     *
     * @param authentication the authentication object containing the user's credentials
     * @return a {@link ResponseEntity} containing a list of {@link PDFFileResponse} if authenticated,
     *         or a 401 Unauthorized response if not authenticated
     */
    @GetMapping("/my-files")
    public ResponseEntity<List<PDFFileResponse>> getMyFiles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String email = authentication.getName(); // This is now the email
        List<PDFFile> userPdfs = pdfRepository.findByUploadedBy(email);
        return ResponseEntity.ok(userPdfs.stream().map(pdf -> new PDFFileResponse(pdf)).toList());
    }
    /**
     * Searches for PDF files by filename across all users.
     *
     * <p>This endpoint allows users to search for PDF files by their filenames, regardless of the uploader.
     * It returns a list of {@link PdfSearchResult} objects containing the ID, filename, uploader's email,
     * upload time, and a link to download the PDF.</p>
     *
     * @param query the search query to filter filenames (case-insensitive)
     * @return a {@link ResponseEntity} containing a list of {@link PdfSearchResult} objects
     */
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

    /**
     * Retrieves the details of a specific PDF file by its ID.
     *
     * <p>This endpoint fetches the PDF file's metadata and associated comments based on the provided ID.
     * If the PDF file is not found, it returns a 404 Not Found response.</p>
     *
     * @param id the ID of the PDF file to retrieve
     * @return a {@link ResponseEntity} containing the PDF details and comments, or a 404 Not Found response
     */
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
/**
     * Downloads a PDF file for the authenticated user.
     *
     * <p>This endpoint allows users to download a PDF file by its ID, provided they are authenticated
     * and the file belongs to them. If the user is not authenticated or does not own the file,
     * appropriate error responses are returned.</p>
     *
     * @param id the ID of the PDF file to download
     * @param authentication the authentication object containing the user's credentials
     * @return a {@link ResponseEntity} containing the PDF file as a resource, or an error response
     */
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
            Path filePathObj = Paths.get(pdfFile.getFilepath()); 
                                                              
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

    /**
     * Adds a comment to a specific PDF file.
     *
     * <p>This endpoint allows authenticated users to add comments to a PDF file by its ID.
     * The comment is associated with the PDF and includes the username of the commenter,
     * the comment text, and the time of the comment.</p>
     *
     * @param id the ID of the PDF file to which the comment is being added
     * @param commentRequest the request body containing the comment text
     * @param auth the authentication object containing the user's credentials
     * @return a {@link ResponseEntity} containing the created comment or a 404 Not Found response if the PDF does not exist
     */
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


    /**
     * Shares a PDF file by generating a shareable link.
     * <p>
     * This endpoint allows the authenticated owner of a PDF to generate a unique shareable link
     * for the specified PDF file. The link can be distributed to others for access.
     * </p>
     *
     * @param id              the ID of the PDF file to share
     * @param authentication  the authentication object representing the current user
     * @return a ResponseEntity containing the shareable link and token if successful,
     *         or an error response if the user is not authenticated, not authorized,
     *         or the PDF file does not exist
     */
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

    /**
     * Handles HTTP GET requests to view a shared PDF file using a share token.
     * <p>
     * This endpoint allows users to access a PDF file that has been shared with them via a unique share token.
     * If the share token is valid and the corresponding PDF file exists and is readable, the PDF is returned
     * as an inline resource for viewing in the browser.
     * </p>
     *
     * @param shareToken the unique token associated with the shared PDF file
     * @return a {@link ResponseEntity} containing the PDF resource if found and accessible,
     *         a 404 Not Found response if the token is invalid or the file is missing,
     *         or a 500 Internal Server Error if there is an issue accessing the file
     */
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
