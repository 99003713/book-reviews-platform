package com.learning.books.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class RatingDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private Integer rating;
    private Instant createdAt;
}
