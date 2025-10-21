package com.learning.books.service.impl;

import com.learning.books.dto.book.TopRatedBookDto;
import com.learning.books.dto.rating.RatingDto;
import com.learning.books.entity.Book;
import com.learning.books.entity.UserBookRating;
import com.learning.books.exception.ResourceNotFoundException;
import com.learning.books.repository.BookRepository;
import com.learning.books.repository.UserBookRatingRepository;
import com.learning.books.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final BookRepository bookRepository;
    private final UserBookRatingRepository ratingRepository;

    /**
     * Add or update rating for a book by a user.
     * Upsert semantics: if a rating exists for (userId, bookId) update it; otherwise insert new.
     */
    @Override
    @Transactional
    public RatingDto addOrUpdateRating(Long bookId, Long userId, Integer ratingValue) {
        log.info("addOrUpdateRating: bookId={} userId={} rating={}", bookId, userId, ratingValue);

//        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
//            throw new IllegalArgumentException("rating must be between 1 and 5");
//        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        // Upsert: find existing rating by user/book
        Optional<UserBookRating> existingOpt = ratingRepository.findByBookIdAndUserId(bookId, userId);

        UserBookRating saved;
        if (existingOpt.isPresent()) {
            UserBookRating existing = existingOpt.get();
            existing.setRating(ratingValue);
            saved = ratingRepository.save(existing);
        } else {
            UserBookRating newRating = UserBookRating.builder()
                    .book(book)
                    .userId(userId)
                    .rating(ratingValue)
                    .createdAt(Instant.now())
                    .build();
            saved = ratingRepository.save(newRating);
        }

        // Map to DTO (assumes RatingDto has matching constructor)
        return new RatingDto(
                saved.getId(),
                saved.getUserId(),
                saved.getBook().getId(),
                saved.getRating(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopRatedBookDto> getTopRatedBooksByGenre(String genre, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ratingRepository.findTopRatedByGenre(genre, PageRequest.of(0, safeLimit));
    }
}