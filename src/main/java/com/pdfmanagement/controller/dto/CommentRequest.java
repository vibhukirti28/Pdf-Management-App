package com.pdfmanagement.controller.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing a request to create or update a comment.
 * Contains the text content of the comment.
 */
@Getter
@Setter
public class CommentRequest {
    private String text;
}
