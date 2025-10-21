package com.learning.books.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response returned by REST APIs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    /**
     * Optional map of field->message (for validation errors).
     */
    private Map<String, String> fields;

    /**
     * Optional trace id to correlate logs and responses.
     */
    private String traceId;
}
