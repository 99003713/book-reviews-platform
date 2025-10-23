package com.learning.books.service.impl;

import com.learning.books.dto.review.ReviewDto;
import com.learning.books.entity.Book;
import com.learning.books.entity.UserBookReview;
import com.learning.books.exception.ConflictException;
import com.learning.books.exception.ResourceNotFoundException;
import com.learning.books.repository.BookRepository;
import com.learning.books.repository.UserBookReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService; // the class under test

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Sample")
                .author("Author Name")
                .genre("Fiction")
                .build();
    }

    @Test
    void addReview_happyPath_savesAndReturnsDto() {
        // arrange
        Long bookId = 1L;
        Long userId = 42L;
        String comment = "Great book!";

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(sampleBook));
        when(reviewRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.empty());

        // simulate save (set id and createdAt)
        ArgumentCaptor<UserBookReview> captor = ArgumentCaptor.forClass(UserBookReview.class);
        when(reviewRepository.save(captor.capture())).thenAnswer(invocation -> {
            UserBookReview r = invocation.getArgument(0);
            r.setId(100L);
            r.setCreatedAt(Instant.now());
            return r;
        });

        // act
        ReviewDto result = reviewService.addReviewToBookByUser(bookId, userId, comment);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getComment()).isEqualTo(comment);
        assertThat(result.getCreatedAt()).isNotNull();

        // verify interactions
        verify(bookRepository, times(1)).findById(bookId);
        verify(reviewRepository, times(1)).findByBookIdAndUserId(bookId, userId);
        verify(reviewRepository, times(1)).save(any(UserBookReview.class));
    }

    @Test
    void addReview_bookNotFound_throwsResourceNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReviewToBookByUser(999L, 1L, "x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void addReview_duplicateReview_throwsConflict() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(reviewRepository.findByBookIdAndUserId(1L, 2L))
                .thenReturn(Optional.of(UserBookReview.builder().id(10L).userId(2L).book(sampleBook).comment("old").build()));

        assertThatThrownBy(() -> reviewService.addReviewToBookByUser(1L, 2L, "new"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Review already exists");

        verify(reviewRepository, never()).save(any());
    }
}