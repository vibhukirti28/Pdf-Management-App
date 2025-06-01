package com.pdfmanagement.controller.dto;

import com.pdfmanagement.model.Comment;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for representing a comment response.
 * <p>
 * This class encapsulates the details of a comment, including its ID, text,
 * the username of the commenter, and the time the comment was made.
 * </p>
 *
 * <p>
 * Typically used to transfer comment data from the backend to the client.
 * </p>
 *
 * @author YourName
 */
@Getter
@Setter
public class CommentResponse {
    private Long id;
    private String text;
    private String username;
    private LocalDateTime commentTime;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.username = comment.getUsername();
        this.commentTime = comment.getCommentTime();
    }
}
