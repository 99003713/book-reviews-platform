package com.learning.books.repository;

import com.learning.books.entity.UserBookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBookReviewRepository extends JpaRepository<UserBookReview, Long> {
    Optional<UserBookReview> findByBookIdAndUserId(Long bookId, Long userId);
}

