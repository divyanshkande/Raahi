package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.exception.custom.AiServiceException;

/**
 * Thrown when AI itinerary modification fails. Caught by the controller and
 * turned into a clean ErrorResponse - never exposes the underlying cause to
 * the client.
 */
public class ItineraryOperationException extends AiServiceException {
    public ItineraryOperationException(String message) {
        super(message);
    }

    public ItineraryOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}