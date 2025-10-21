package com.learning.books.service;

import com.learning.books.dto.review.ReviewDto;

public interface ReviewService {
    ReviewDto addReviewToBookByUser(Long bookId, Long userId, String comment);
}
