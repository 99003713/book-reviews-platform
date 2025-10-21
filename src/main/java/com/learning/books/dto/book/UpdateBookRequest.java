package com.learning.books.dto.book;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
//partial update for book fields
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookRequest {

    // all fields optional for PATCH-like update; validation (if any) can be added per field
    @Size(min = 1, message = "title must not be empty")
    private String title;

    @Size(min = 1, message = "author must not be empty")
    private String author;

    private String description;

    private String genre;

    private LocalDate publishDate;
}

