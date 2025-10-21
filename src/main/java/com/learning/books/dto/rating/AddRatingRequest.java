package com.learning.books.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddRatingRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}
