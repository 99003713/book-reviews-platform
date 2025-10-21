package com.learning.books.controller;

import com.learning.books.dto.book.TopRatedBookDto;
import com.learning.books.dto.rating.AddRatingRequest;
import com.learning.books.dto.rating.RatingDto;
import com.learning.books.dto.common.ApiResponse;
import com.learning.books.security.CustomUserDetails;
import com.learning.books.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * Add or update rating for a book by the authenticated user.
     */
    @PostMapping("/books/rating/{bookId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<ApiResponse<RatingDto>> addOrUpdateRating(
            @PathVariable Long bookId,
            @Valid @RequestBody AddRatingRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Long userId = currentUser.getId();
        log.info("POST /api/v1/books/rating/{} by user={}", bookId, userId);

        RatingDto dto = ratingService.addOrUpdateRating(bookId, userId, request.getRating());

        ApiResponse<RatingDto> resp = ApiResponse.<RatingDto>builder()
                .success(true)
                .message("Rating saved")
                .data(dto)
                .build();

        return ResponseEntity.ok(resp);
    }

    /**
     * Get top rated books in a genre (public).
     */
    @GetMapping("/genres/top-rated/{genre}")
    public ResponseEntity<ApiResponse<List<TopRatedBookDto>>> getTopRatedByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "5") int limit) {

        log.debug("GET /api/v1/genres/top-rated/{}?limit={}", genre, limit);
        List<TopRatedBookDto> list = ratingService.getTopRatedBooksByGenre(genre, limit);

        ApiResponse<List<TopRatedBookDto>> resp = ApiResponse.<List<TopRatedBookDto>>builder()
                .success(true)
                .message("Top rated books fetched")
                .data(list)
                .build();

        return ResponseEntity.ok(resp);
    }
}