package com.learning.books.dto.book;
//response object returned by APIs


import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BookDto {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String genre;
    private LocalDate publishDate;
    private Instant createdAt;
    private Instant updatedAt;
}

