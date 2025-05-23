package com.pdfmanagement.controller;

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
    @GetMapping("/access/{shareToken}")
    public ResponseEntity<?> accessSharedPdf(@PathVariable String shareToken) {
        var sharedOpt = sharedFileRepository.findByShareToken(shareToken);
        if (sharedOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SharedFile sharedFile = sharedOpt.get();
        PDFFile pdfFile = sharedFile.getPdfFile();
        List<Comment> comments = commentRepository.findByPdfFile(pdfFile);

        return ResponseEntity.ok(new PdfDetailsResponse(pdfFile, comments, "/api/shared/download/" + shareToken));
    }

    // Download shared PDF by share token (no auth)
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

    // DTO for PDF metadata + comments + download URL
    public static class PdfDetailsResponse {
        private PDFFile pdfFile;
        private List<Comment> comments;
        private String pdfUrl;

        public PdfDetailsResponse(PDFFile pdfFile, List<Comment> comments, String pdfUrl) {
            this.pdfFile = pdfFile;
            this.comments = comments;
            this.pdfUrl = pdfUrl;
        }

        public PDFFile getPdfFile() { return pdfFile; }
        public List<Comment> getComments() { return comments; }
        public String getPdfUrl() { return pdfUrl; }
    }

    @PostMapping("/access/{shareToken}/comment")
public ResponseEntity<?> addGuestComment(@PathVariable String shareToken, @RequestBody GuestCommentRequest commentRequest) {
    var sharedOpt = sharedFileRepository.findByShareToken(shareToken);
    if (sharedOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    SharedFile sharedFile = sharedOpt.get();
    PDFFile pdfFile = sharedFile.getPdfFile();

    Comment comment = new Comment();
    comment.setPdfFile(pdfFile);
    comment.setUsername(commentRequest.getUsername());
    comment.setText(commentRequest.getText());
    comment.setCommentTime(java.time.LocalDateTime.now());

    commentRepository.save(comment);

    return ResponseEntity.ok(java.util.Map.of("message", "Comment added"));
}
public static class GuestCommentRequest {
    private String username;
    private String text;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}

}
