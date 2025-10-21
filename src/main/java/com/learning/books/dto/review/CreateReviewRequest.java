package com.learning.books.dto.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotBlank
    private String comment;
}

