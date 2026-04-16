package com.smartcampus.exception;

/**
 * Exception thrown when an attempt is made to delete a Room
 * that still has sensors assigned to it.
 *
 * Mapped to HTTP 409 Conflict via RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
