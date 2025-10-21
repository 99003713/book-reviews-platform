package com.learning.books.controller;

import com.learning.books.dto.common.ApiResponse;
import com.learning.books.dto.review.CreateReviewRequest;
import com.learning.books.dto.review.ReviewDto;
import com.learning.books.security.CustomUserDetails;
import com.learning.books.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Add a review to a book.
     * Any authenticated user may call this, but service will prevent book authors from reviewing their own book.
     */
    @PostMapping("/reviews/{bookId}")
//    @PreAuthorize("isAuthenticated()")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<ApiResponse<ReviewDto>> addReview(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long bookId,
            @Valid @RequestBody CreateReviewRequest request) {

        Long userId = currentUser == null ? null : currentUser.getId();
        log.info("User={} adding review for bookId={}", userId, bookId);

        // Delegate to service (service should enforce business rules & throw domain exceptions)
        ReviewDto created = reviewService.addReviewToBookByUser(bookId, userId, request.getComment());

        ApiResponse<ReviewDto> resp = ApiResponse.<ReviewDto>builder()
                .success(true)
                .message("Review added successfully")
                .data(created)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}