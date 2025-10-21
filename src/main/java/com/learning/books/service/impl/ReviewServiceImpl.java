package com.learning.books.service.impl;

import com.learning.books.dto.review.ReviewDto;
import com.learning.books.entity.Book;
import com.learning.books.entity.UserBookReview;
import com.learning.books.exception.ConflictException;
import com.learning.books.exception.ResourceNotFoundException;
import com.learning.books.repository.BookRepository;
import com.learning.books.repository.UserBookReviewRepository;
import com.learning.books.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final BookRepository bookRepository;
    private final UserBookReviewRepository reviewRepository;

    /**
     * Add a review for a book by a user.
     * Behavior:
     *  - If book not found -> ResourceNotFoundException (404)
     *  - If user already reviewed the book -> ConflictException (409)
     *  - Otherwise create review and return DTO (201)
     */
    @Override
    @Transactional
    public ReviewDto addReviewToBookByUser(Long bookId, Long userId, String comment) {
        log.info("addReview: bookId={} userId={}", bookId, userId);

        // 1) ensure book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        // 2) check if user already reviewed this book
        boolean alreadyReviewed = reviewRepository.findByBookIdAndUserId(bookId, userId).isPresent();
        if (alreadyReviewed) {
            log.warn("addReview: duplicate attempt bookId={} userId={}", bookId, userId);
            throw new ConflictException("Review already exists for this user and book.");
        }

        // 3) create & save
        UserBookReview review = UserBookReview.builder()
                .book(book)
                .userId(userId)
                .comment(comment)
                .build();

        UserBookReview saved = reviewRepository.save(review);

        // 4) map to DTO and return
        // Assuming ReviewDto has constructor: ReviewDto(Long id, Long userId, Long bookId, String comment, Instant createdAt)
        return new ReviewDto(
                saved.getId(),
                saved.getUserId(),
                saved.getBook().getId(),
                saved.getComment(),
                saved.getCreatedAt()
        );
    }
}