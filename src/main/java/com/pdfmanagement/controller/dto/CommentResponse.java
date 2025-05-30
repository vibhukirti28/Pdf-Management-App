package com.pdfmanagement.controller.dto;

import com.pdfmanagement.model.Comment;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

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
