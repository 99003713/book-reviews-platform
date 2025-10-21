package com.learning.books.service;

import com.learning.books.dto.book.TopRatedBookDto;
import com.learning.books.dto.rating.RatingDto;

import java.util.List;

public interface RatingService {
    RatingDto addOrUpdateRating(Long bookId, Long userId, Integer rating);
    List<TopRatedBookDto> getTopRatedBooksByGenre(String genre, int limit);
}

