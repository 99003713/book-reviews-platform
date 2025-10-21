package com.learning.books.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.learning.books.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    record ErrorResponse(Instant timestamp,
                         int status,
                         String error,
                         String message,
                         Map<String, String> fields,
                         String traceId) { }

    private String traceIdFromMdcOrRequest() {
        // if you store traceId in MDC, read it; otherwise omit or build an UUID
        return java.util.UUID.randomUUID().toString();
    }

    // 1) Handle Jackson invalid format (enum parse errors included)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                      HttpServletRequest req) {
        Throwable cause = ex.getCause();
        String message = "Malformed JSON request";
        Map<String, String> fields = null;

        if (cause instanceof InvalidFormatException ife) {
            // example: Invalid value "xyz" for Enum Role
            String targetType = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "Unknown";
            // derive field name path (may contain path like [role])
            String path = ife.getPath() != null && !ife.getPath().isEmpty()
                    ? ife.getPath().stream().map(p -> p.getFieldName()).filter(Objects::nonNull).collect(Collectors.joining("."))
                    : null;

            String invalidValue = Optional.ofNullable(ife.getValue()).map(Object::toString).orElse("unknown");
            message = String.format("Invalid value '%s' for type %s", invalidValue, targetType);

            // If it's an enum, list allowed values
            Class<?> target = ife.getTargetType();
            if (target != null && target.isEnum()) {
                String allowed = Arrays.stream(target.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                message = String.format("Invalid value '%s' for field '%s'. Allowed values: [%s]",
                        invalidValue, path == null ? "<unknown>" : path, allowed);
            }

            if (path != null) {
                fields = Map.of(path, message);
            }
        } else {
            // generic message
            message = ex.getMessage();
        }

        log.warn("Bad request: {} - {}", req.getRequestURI(), message);
        ErrorResponse err = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), message, fields, traceIdFromMdcOrRequest());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    // 2) Handle @Valid validation errors (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 3) Optionally handle IllegalArgumentException to return 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        String msg = ex.getMessage();
        ErrorResponse err = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), msg, null, traceIdFromMdcOrRequest());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied for {}: {}", req.getRequestURI(), ex.getMessage());

        ApiResponse<Object> body = ApiResponse.<Object>builder()
                .success(false)
                .message("Forbidden: you don't have permission to access this resource")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 🟡 Handle not found exceptions (for any entity)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiResponse<Object> resp = ApiResponse.<Object>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    // --- ConflictException -> 409 ---
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(ConflictException ex, HttpServletRequest req) {
        log.warn("Conflict on {}: {}", req.getRequestURI(), ex.getMessage());
        ApiResponse<Object> resp = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
    }

    // 4) Fallback — log and return generic 500 (keeps traceId)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception for {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse err = new ErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "An unexpected error occurred", null,
                traceIdFromMdcOrRequest());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}