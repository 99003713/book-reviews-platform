package com.learning.books.repository;

import com.learning.books.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Simple text search on title (case-insensitive, partial match)
     * Example usage: bookRepository.findByTitleContainingIgnoreCase("harry", PageRequest.of(0, 10));
     */
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Search by author name (partial, case-insensitive)
     */
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    /**
     * Search by exact genre (useful for listing by genre)
     */
    Page<Book> findByGenre(String genre, Pageable pageable);

    // You can add more query methods as needed, e.g. findByPublishDateBetween(...)
}
