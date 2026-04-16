package com.smartcampus.exception;

/**
 * Exception thrown when a reading is attempted on a sensor
 * that is currently in MAINTENANCE mode.
 *
 * Mapped to HTTP 403 Forbidden via SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
