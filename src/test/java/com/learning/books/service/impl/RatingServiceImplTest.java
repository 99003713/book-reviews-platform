package com.learning.books.service.impl;

import com.learning.books.dto.rating.RatingDto;
import com.learning.books.entity.Book;
import com.learning.books.entity.UserBookRating;
import com.learning.books.repository.BookRepository;
import com.learning.books.repository.UserBookRatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RatingServiceImpl (pure Mockito, no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookRatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Sample Book")
                .author("Author")
                .genre("Self-Help")
                .build();
    }

    @Test
    void addOrUpdateRating_happyPath_insertsNewRating() {
        Long bookId = 1L;
        Long userId = 5L;
        Integer ratingValue = 4;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(sampleBook));
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.empty());

        ArgumentCaptor<UserBookRating> captor = ArgumentCaptor.forClass(UserBookRating.class);
        when(ratingRepository.save(captor.capture())).thenAnswer(invocation -> {
            UserBookRating r = invocation.getArgument(0);
            r.setId(200L);
            r.setCreatedAt(Instant.now());
            return r;
        });

        RatingDto dto = ratingService.addOrUpdateRating(bookId, userId, ratingValue);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(200L);
        assertThat(dto.getBookId()).isEqualTo(bookId);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getRating()).isEqualTo(ratingValue);

        verify(bookRepository, times(1)).findById(bookId);
        verify(ratingRepository, times(1)).findByBookIdAndUserId(bookId, userId);
        verify(ratingRepository, times(1)).save(any(UserBookRating.class));
    }

    @Test
    void addOrUpdateRating_existingRating_updatesRating() {
        Long bookId = 1L;
        Long userId = 5L;
        Integer newRating = 2;

        UserBookRating existing = UserBookRating.builder()
                .id(300L)
                .book(sampleBook)
                .userId(userId)
                .rating(5)
                .createdAt(Instant.now())
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(sampleBook));
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(UserBookRating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RatingDto dto = ratingService.addOrUpdateRating(bookId, userId, newRating);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(300L);
        assertThat(dto.getRating()).isEqualTo(newRating);

        // ensure save called on the existing entity
        verify(ratingRepository, times(1)).save(existing);
    }

    @Test
    void addOrUpdateRating_invalidRating_throwsIllegalArgument() {
        Long bookId = 1L;
        Long userId = 5L;

        // NOTE: DO NOT stub bookRepository here — validation happens before repository calls.
        // when(bookRepository.findById(bookId)).thenReturn(Optional.of(sampleBook)); // <-- removed

        assertThatThrownBy(() -> ratingService.addOrUpdateRating(bookId, userId, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating must be between 1 and 5");

        assertThatThrownBy(() -> ratingService.addOrUpdateRating(bookId, userId, 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating must be between 1 and 5");

        // repositories must not be touched for invalid input
        verifyNoInteractions(bookRepository, ratingRepository);
    }
}