package com.pdfmanagement.controller;

import com.pdfmanagement.controller.dto.PdfDetailsResponse;
import com.pdfmanagement.model.Comment;
import com.pdfmanagement.model.PDFFile;
import com.pdfmanagement.model.SharedFile;
import com.pdfmanagement.repository.CommentRepository;
import com.pdfmanagement.repository.PDFRepository;
import com.pdfmanagement.repository.SharedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/shared")
public class SharedFileController {

    @Autowired
    private SharedFileRepository sharedFileRepository;

    @Autowired
    private PDFRepository pdfRepository;

    @Autowired
    private CommentRepository commentRepository;

    // Generate shareable link for a PDF (requires auth)
    /**
     * Generates a shareable link for a PDF file identified by its ID.
     * <p>
     * This endpoint creates a new {@link SharedFile} entry associated with the specified PDF file,
     * and returns a shareable URL that can be used to access the file.
     * </p>
     *
     * @param pdfId the ID of the PDF file to generate a share link for
     * @param auth the authentication object representing the current user
     * @return a {@link ResponseEntity} containing the generated share URL if successful,
     *         or a 404 Not Found response if the PDF file does not exist
     */
    @PostMapping("/generate/{pdfId}")
    public ResponseEntity<?> generateShareLink(@PathVariable Long pdfId, Authentication auth) {
        var pdfOpt = pdfRepository.findById(pdfId);
        if (pdfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PDFFile pdfFile = pdfOpt.get();
        // TODO: Optionally verify auth.getName() is owner or allowed to share

        SharedFile sharedFile = new SharedFile();
        sharedFile.setPdfFile(pdfFile);
        sharedFileRepository.save(sharedFile);

        String shareUrl = "/api/shared/access/" + sharedFile.getShareToken();

        return ResponseEntity.ok(java.util.Map.of("shareUrl", shareUrl));
    }

    // Access shared PDF metadata + comments by share token (no auth)
    /**
     * Handles HTTP GET requests to access a shared PDF file using a share token.
     * <p>
     * This endpoint retrieves the shared PDF file and its associated comments if the provided
     * share token is valid. If the share token does not correspond to any shared file, a 404 Not Found
     * response is returned.
     * </p>
     *
     * @param shareToken the unique token used to access the shared PDF file
     * @return a {@link ResponseEntity} containing a {@link PdfDetailsResponse} with the PDF file and its comments,
     *         or a 404 Not Found response if the token is invalid
     */
    @GetMapping("/access/{shareToken}")
    public ResponseEntity<?> accessSharedPdf(@PathVariable String shareToken) {
        var sharedOpt = sharedFileRepository.findByShareToken(shareToken);
        if (sharedOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SharedFile sharedFile = sharedOpt.get();
        PDFFile pdfFile = sharedFile.getPdfFile();
        List<Comment> comments = commentRepository.findByPdfFile(pdfFile);

        return ResponseEntity.ok(new PdfDetailsResponse(pdfFile, comments));
    }

    // Download shared PDF by share token (no auth)
    /**
     * Handles HTTP GET requests for downloading a shared PDF file using a share token.
     * <p>
     * This endpoint retrieves a shared PDF file associated with the provided share token.
     * If the token is valid and the file exists and is readable, the PDF is returned as an inline resource.
     * Otherwise, a 404 Not Found or 500 Internal Server Error response is returned as appropriate.
     * </p>
     *
     * @param shareToken the unique token identifying the shared PDF file
     * @return a {@link ResponseEntity} containing the PDF file as a {@link Resource} if found and accessible,
     *         or an appropriate HTTP error response if not found or an error occurs
     */
    @GetMapping("/download/{shareToken}")
    public ResponseEntity<Resource> downloadSharedPdf(@PathVariable String shareToken) {
        var sharedOpt = sharedFileRepository.findByShareToken(shareToken);
        if (sharedOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SharedFile sharedFile = sharedOpt.get();
        PDFFile pdfFile = sharedFile.getPdfFile();

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

    // Add a comment to a shared PDF (no auth, uses shareToken)
    /**
     * Adds a comment to a shared PDF file using the provided share token.
     * <p>
     * This endpoint allows a guest user to add a comment to a PDF file that has been shared via a unique share token.
     * The comment details, including the username and text, are provided in the request body.
     * </p>
     *
     * @param shareToken      the unique token identifying the shared file
     * @param commentRequest  the request body containing the username and comment text
     * @return a ResponseEntity containing a success message if the comment is added,
     *         or a 404 Not Found response if the shared file does not exist
     */
    @PostMapping("/{shareToken}/comments")
    public ResponseEntity<?> addSharedFileComment(@PathVariable String shareToken,
            @RequestBody GuestCommentRequest commentRequest) {
        var sharedOpt = sharedFileRepository.findByShareToken(shareToken);
        if (sharedOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SharedFile sharedFile = sharedOpt.get();
        PDFFile pdfFile = sharedFile.getPdfFile();

        Comment comment = new Comment();
        comment.setPdfFile(pdfFile);
        comment.setUsername(commentRequest.getUsername()); // Set the username from the request
        comment.setText(commentRequest.getText());
        comment.setCommentTime(java.time.LocalDateTime.now()); // Ensure time is set

        commentRepository.save(comment);

        return ResponseEntity.ok(java.util.Map.of("message", "Comment added"));
    }

    /**
     * Represents a request to add a comment from a guest user.
     * Contains the username of the guest and the comment text.
     *
     * <p>
     * Example usage:
     * <pre>
     *     GuestCommentRequest request = new GuestCommentRequest();
     *     request.setUsername("guestUser");
     *     request.setText("This is a comment.");
     * </pre>
     * </p>
     *
     * @author YourName
     */
    public static class GuestCommentRequest {
        private String username;
        private String text;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}
