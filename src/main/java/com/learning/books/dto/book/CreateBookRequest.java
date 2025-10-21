package com.learning.books.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

//incoming payload for creating a book
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookRequest {

    @NotBlank(message = "title must not be blank")
    private String title;

    @NotBlank(message = "author must not be blank")
    private String author;

    // optional
    private String description;

    @NotBlank(message = "genre must not be blank")
    private String genre;

    @NotNull(message = "publishDate must not be null")
    private LocalDate publishDate;
}
