package com.smartcampus.exception;

/**
 * Custom exception for resources not found in the DataStore.
 *
 * Mapped to HTTP 404 Not Found via ResourceNotFoundExceptionMapper.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
