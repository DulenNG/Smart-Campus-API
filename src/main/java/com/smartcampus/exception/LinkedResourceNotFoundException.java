package com.smartcampus.exception;

/**
 * Exception thrown when a resource refers to a parent or dependency
 * that does not exist (e.g., adding a sensor to a non-existent room).
 *
 * Mapped to HTTP 422 Unprocessable Entity via LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
