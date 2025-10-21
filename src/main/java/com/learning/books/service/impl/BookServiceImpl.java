package com.learning.books.service.impl;

import com.learning.books.dto.book.BookDto;
import com.learning.books.dto.book.CreateBookRequest;
import com.learning.books.dto.book.UpdateBookRequest;
import com.learning.books.entity.Book;
import com.learning.books.exception.ResourceNotFoundException;
import com.learning.books.repository.BookRepository;
import com.learning.books.service.BookService;
import com.learning.books.service.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BookDto createBook(CreateBookRequest request) {
        log.info("createBook: title='{}', author='{}'", request.getTitle(), request.getAuthor());
        Book entity = BookMapper.toEntity(request);
        Book saved = bookRepository.save(entity);
        log.info("createBook: saved id={}", saved.getId());
        return BookMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto getBookById(Long id) {
        log.debug("getBookById: id={}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("getBookById: not found id={}", id);
                    return new ResourceNotFoundException("Book not found with id: " + id);
                });
        return BookMapper.toDto(book);
    }

    @Override
    @Transactional
    public BookDto updateBook(Long id, UpdateBookRequest request) {
        log.info("updateBook: id={}, fields present title={}, author={}, genre={}",
                id,
                request.getTitle() != null,
                request.getAuthor() != null,
                request.getGenre() != null);

        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("updateBook: not found id={}", id);
                    return new ResourceNotFoundException("Book not found with id: " + id);
                });

        BookMapper.updateEntityFromDto(request, existing);
        Book updated = bookRepository.save(existing);
        log.info("updateBook: updated id={}", updated.getId());
        return BookMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        log.info("deleteBook: id={}", id);
        if (!bookRepository.existsById(id)) {
            log.warn("deleteBook: not found id={}", id);
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        log.info("deleteBook: deleted id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> searchBooks(String title,
                                     String author,
                                     String genre,
                                     LocalDate publishDateFrom,
                                     LocalDate publishDateTo,
                                     Pageable pageable) {
        log.debug("searchBooks: title='{}', author='{}', genre='{}', from='{}', to='{}', page={}, size={}",
                title, author, genre, publishDateFrom, publishDateTo,
                pageable.getPageNumber(), pageable.getPageSize());

        Specification<Book> spec = buildCombinedSpecification(title, author, genre, publishDateFrom, publishDateTo);
        Page<BookDto> result = bookRepository.findAll(spec, pageable).map(BookMapper::toDto);

        log.debug("searchBooks: returned {}, total={}", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    /**
     * Build a combined Specification for optional filters.
     * - title, author -> case-insensitive partial match
     * - genre -> exact match (trimmed)
     * - publishDateFrom/to -> inclusive range
     */
    private Specification<Book> buildCombinedSpecification(String title,
                                                           String author,
                                                           String genre,
                                                           LocalDate from,
                                                           LocalDate to) {
        Specification<Book> spec = Specification.where(null);

        if (title != null && !title.isBlank()) {
            String pattern = "%" + title.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern));
        }

        if (author != null && !author.isBlank()) {
            String pattern = "%" + author.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("author")), pattern));
        }

        if (genre != null && !genre.isBlank()) {
            String g = genre.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("genre"), g));
        }

        if (from != null && to != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("publishDate"), from, to));
        } else if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("publishDate"), from));
        } else if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("publishDate"), to));
        }

        return spec;
    }
}