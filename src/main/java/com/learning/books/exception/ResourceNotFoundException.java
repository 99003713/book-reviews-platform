package com.learning.books.exception;

/**
 * Simple runtime exception used when a requested resource is not found.
 * We'll map this to HTTP 404 in a ControllerAdvice later.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

