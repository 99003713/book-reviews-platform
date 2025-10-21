package com.learning.books.service;

import com.learning.books.dto.book.BookDto;
import com.learning.books.dto.book.CreateBookRequest;
import com.learning.books.dto.book.UpdateBookRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service contract for Book-related business operations.
 * Single, flexible search method (searchBooks) handles all filtering.
 */
public interface BookService {

    BookDto createBook(CreateBookRequest request);

    BookDto getBookById(Long id);

    BookDto updateBook(Long id, UpdateBookRequest request);

    void deleteBook(Long id);

    /**
     * Flexible search supporting title (partial), author (partial), genre (exact),
     * and publish date range (inclusive). Any combination of filters may be provided.
     *
     * @param title            partial title (case-insensitive) or null
     * @param author           partial author (case-insensitive) or null
     * @param genre            exact genre match or null
     * @param publishDateFrom  start date inclusive or null
     * @param publishDateTo    end date inclusive or null
     * @param pageable         paging and sorting
     * @return page of BookDto
     */
    Page<BookDto> searchBooks(String title,
                              String author,
                              String genre,
                              LocalDate publishDateFrom,
                              LocalDate publishDateTo,
                              Pageable pageable);
}