package com.learning.books.repository;


import com.learning.books.dto.book.TopRatedBookDto;
import com.learning.books.entity.UserBookRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRatingRepository extends JpaRepository<UserBookRating, Long> {

    Optional<UserBookRating> findByBookIdAndUserId(Long bookId, Long userId);


    @Query("""
        SELECT new com.learning.books.dto.book.TopRatedBookDto(
            b.id,
            b.title,
            b.author,
            b.genre,
            AVG(r.rating),
            COUNT(r.id)
        )
        FROM UserBookRating r
        JOIN r.book b
        WHERE b.genre = :genre
        GROUP BY b.id, b.title, b.author, b.genre
        ORDER BY AVG(r.rating) DESC, COUNT(r.id) DESC
        """)
    List<TopRatedBookDto> findTopRatedByGenre(String genre, Pageable pageable);
}

