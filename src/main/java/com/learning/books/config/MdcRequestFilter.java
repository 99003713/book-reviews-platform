package com.learning.books.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * Simple filter that adds a request "traceId" into MDC for log correlation.
 * It ensures logs and error responses contain the same trace id.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcRequestFilter implements Filter {

    public static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = null;
        try {
            // Try to reuse existing trace id from incoming header (if client provides)
            if (request instanceof HttpServletRequest) {
                HttpServletRequest http = (HttpServletRequest) request;
                traceId = http.getHeader("X-Request-Id");
            }

            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }

            MDC.put(TRACE_ID_KEY, traceId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}

