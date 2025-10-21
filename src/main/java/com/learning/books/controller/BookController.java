package com.learning.books.controller;

import com.learning.books.dto.book.BookDto;
import com.learning.books.dto.book.CreateBookRequest;
import com.learning.books.dto.book.UpdateBookRequest;
import com.learning.books.dto.common.ApiResponse;
import com.learning.books.security.CustomUserDetails;
import com.learning.books.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Modification Apis", description = "Book Modification Apis")
public class BookController {

    private final BookService bookService;

    /**
     * Create a new book.
     * Only AUTHOR or ADMIN may call this.
     * The authenticated user will be used as the book owner (service should set owner).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<ApiResponse<BookDto>> addBook(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateBookRequest request) {

        log.info("User={} requested create book title='{}'", currentUser.getId(), request.getTitle());

        // Pass currentUser id to service so it can set ownership & enforce rules
        // NOTE: update your BookService#createBook signature to accept ownerId:
        //    BookDto createBook(CreateBookRequest req, Long ownerId);
        BookDto created = bookService.createBook(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        ApiResponse<BookDto> resp = ApiResponse.<BookDto>builder()
                .success(true)
                .message("Book created successfully")
                .data(created)
                .build();

        return ResponseEntity.created(location).body(resp);
    }

    /**
     * Get book by id.
     * Any authenticated user can fetch book details.
     * If you want this endpoint to be public, remove @PreAuthorize.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookDto>> getBookById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) {

        log.debug("User={} fetching book id={}", currentUser.getId(), id);
        BookDto dto = bookService.getBookById(id);
        ApiResponse<BookDto> resp = ApiResponse.<BookDto>builder()
                .success(true)
                .message("Book fetched successfully")
                .data(dto)
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * Update book.
     * Only the owner (AUTHOR) or ADMIN should be allowed — service will enforce ownership.
     * Controller passes current user id so service can check.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<ApiResponse<BookDto>> updateBook(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {

        log.info("User={} updating book id={}", currentUser.getId(), id);

        // NOTE: change BookService#updateBook to accept currentUserId:
        //    BookDto updateBook(Long id, UpdateBookRequest request, Long currentUserId);
        BookDto updated = bookService.updateBook(id, request);

        ApiResponse<BookDto> resp = ApiResponse.<BookDto>builder()
                .success(true)
                .message("Book updated successfully")
                .data(updated)
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * Delete book.
     * Only the owner (AUTHOR) or ADMIN should be allowed — service will enforce ownership and idempotency.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) {

        log.info("User={} deleting book id={}", currentUser.getId(), id);
        // NOTE: change BookService#deleteBook to accept currentUserId:
        //    void deleteBook(Long id, Long currentUserId);
        bookService.deleteBook(id);

        ApiResponse<Void> resp = ApiResponse.<Void>builder()
                .success(true)
                .message("Book deleted successfully")
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(resp);
    }

    /**
     * Flexible search endpoint that delegates to service.searchBooks(...)
     *
     * Query examples:
     * GET /api/v1/books/search?title=habits&page=0&size=10&sort=publishDate,desc
     *
     * publishDateFrom / publishDateTo use ISO date: yyyy-MM-dd
     *
     * Note: this is currently protected (authenticated users only). If you want public access,
     * remove @PreAuthorize("isAuthenticated()").
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<BookDto>>> searchBooks(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishDateTo,
            Pageable pageable) {

        log.debug("User={} search title='{}' author='{}' genre='{}' from='{}' to='{}' page={}",
                currentUser.getId(), title, author, genre, publishDateFrom, publishDateTo, pageable);

        Page<BookDto> results = bookService.searchBooks(title, author, genre, publishDateFrom, publishDateTo, pageable);

        ApiResponse<Page<BookDto>> resp = ApiResponse.<Page<BookDto>>builder()
                .success(true)
                .message("Books fetched successfully")
                .data(results)
                .build();
        return ResponseEntity.ok(resp);
    }
}