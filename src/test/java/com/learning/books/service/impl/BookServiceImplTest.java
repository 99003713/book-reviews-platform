package com.learning.books.service.impl;

import com.learning.books.dto.book.BookDto;
import com.learning.books.dto.book.CreateBookRequest;
import com.learning.books.dto.book.UpdateBookRequest;
import com.learning.books.entity.Book;
import com.learning.books.exception.ResourceNotFoundException;
import com.learning.books.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookServiceImpl
 * - Pure Mockito tests (no Spring context)
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Atomic Habits")
                .author("James Clear")
                .description("Practical habit advice")
                .genre("Self-Help")
                .publishDate(LocalDate.of(2018, 10, 16))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void createBook_savesAndReturnsDto() {
        // arrange
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle(sampleBook.getTitle());
        req.setAuthor(sampleBook.getAuthor());
        req.setDescription(sampleBook.getDescription());
        req.setGenre(sampleBook.getGenre());
        req.setPublishDate(sampleBook.getPublishDate());

        // when BookMapper.toEntity(request) creates an entity, service calls repository.save(...)
        // We'll capture the saved entity and return a saved book with id populated
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        when(bookRepository.save(captor.capture())).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setId(10L); // simulate DB generated id
            b.setCreatedAt(Instant.now());
            return b;
        });

        // act
        BookDto dto = bookService.createBook(req);

        // assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isEqualTo(sampleBook.getTitle());
        assertThat(dto.getAuthor()).isEqualTo(sampleBook.getAuthor());
        assertThat(dto.getGenre()).isEqualTo(sampleBook.getGenre());

        Book savedArg = captor.getValue();
        assertThat(savedArg.getTitle()).isEqualTo(sampleBook.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void getBookById_found_returnsDto() {
        // arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        // act
        BookDto dto = bookService.getBookById(1L);

        // assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Atomic Habits");
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void getBookById_notFound_throwsResourceNotFound() {
        // arrange
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");
        verify(bookRepository, times(1)).findById(99L);
    }

    @Test
    void updateBook_existing_updatesFieldsAndReturnsDto() {
        // arrange
        UpdateBookRequest req = new UpdateBookRequest();
        req.setDescription("New description");
        req.setGenre("Productivity");
        req.setPublishDate(LocalDate.of(2019, 1, 1));

        Book existing = Book.builder()
                .id(1L)
                .title("Atomic Habits")
                .author("James Clear")
                .description("old")
                .genre("Self-Help")
                .publishDate(LocalDate.of(2018, 10, 16))
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // act
        BookDto dto = bookService.updateBook(1L, req);

        // assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("New description");
        assertThat(dto.getGenre()).isEqualTo("Productivity");
        assertThat(dto.getPublishDate()).isEqualTo(LocalDate.of(2019, 1, 1));

        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_notFound_throwsResourceNotFound() {
        // arrange
        UpdateBookRequest req = new UpdateBookRequest();
        req.setDescription("x");
        when(bookRepository.findById(5L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> bookService.updateBook(5L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");
        verify(bookRepository, times(1)).findById(5L);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteBook_existing_deletesSuccessfully() {
        // arrange
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        // act
        bookService.deleteBook(1L);

        // assert (no exception) and verify
        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBook_notFound_throwsResourceNotFound() {
        // arrange
        when(bookRepository.existsById(99L)).thenReturn(false);

        // act & assert
        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");
        verify(bookRepository, times(1)).existsById(99L);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchBooks_withFilters_returnsPagedDto() {
        // arrange: create 2 book entities that match
        Book b1 = Book.builder()
                .id(1L)
                .title("Atomic Habits")
                .author("James Clear")
                .genre("Self-Help")
                .publishDate(LocalDate.of(2018, 10, 16))
                .build();

        Book b2 = Book.builder()
                .id(2L)
                .title("Deep Work")
                .author("Cal Newport")
                .genre("Productivity")
                .publishDate(LocalDate.of(2016, 1, 5))
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Book> page = new PageImpl<>(List.of(b1, b2), pageable, 2);

        // We don't assert the exact Specification instance — match any Specification
        when(bookRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(page);

        // act
        Page<BookDto> result = bookService.searchBooks("Atomic", null, null,
                null, null, pageable);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(BookDto::getTitle)
                .contains("Atomic Habits", "Deep Work");

        verify(bookRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }
}