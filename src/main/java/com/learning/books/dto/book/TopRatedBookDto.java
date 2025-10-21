package com.learning.books.dto.book;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopRatedBookDto {
    private Long bookId;
    private String title;
    private String author;
    private String genre;
    private Double averageRating;
    private Long ratingCount;
}
