package com.learning.books.service.mapper;

import com.learning.books.dto.book.BookDto;
import com.learning.books.dto.book.CreateBookRequest;
import com.learning.books.entity.Book;
import com.learning.books.dto.book.UpdateBookRequest;

public final class BookMapper {

    private BookMapper() {}

    public static Book toEntity(CreateBookRequest req) {
        if (req == null) return null;
        return Book.builder()
                .title(req.getTitle())
                .author(req.getAuthor())
                .description(req.getDescription())
                .genre(req.getGenre())
                .publishDate(req.getPublishDate())
                .build();
    }

    public static BookDto toDto(Book entity) {
        if (entity == null) return null;
        return BookDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .description(entity.getDescription())
                .genre(entity.getGenre())
                .publishDate(entity.getPublishDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Apply updates from UpdateBookRequest onto existing entity.
     * Only non-null fields in request will overwrite the entity's fields.
     */
    public static void updateEntityFromDto(UpdateBookRequest req, Book entity) {
        if (req == null || entity == null) return;

        if (req.getTitle() != null) entity.setTitle(req.getTitle());
        if (req.getAuthor() != null) entity.setAuthor(req.getAuthor());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getGenre() != null) entity.setGenre(req.getGenre());
        if (req.getPublishDate() != null) entity.setPublishDate(req.getPublishDate());
    }
}
