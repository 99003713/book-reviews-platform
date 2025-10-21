package com.learning.books.dto.review;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private String comment;
    private Instant createdAt;
}
