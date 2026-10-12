package com.divyansh.aitourplanner.exception;

import com.divyansh.aitourplanner.dto.ErrorResponse;
import com.divyansh.aitourplanner.exception.custom.AiServiceException;
import com.divyansh.aitourplanner.exception.custom.ItineraryValidationException;
import com.divyansh.aitourplanner.exception.custom.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.Set;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleValidationExceptions_returns400WithFieldErrors() {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "email", "must be valid"));
        bindingResult.addError(new FieldError("test", "password", "too short"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).contains("email: must be valid");
        assertThat(response.getBody().getMessage()).contains("password: too short");
    }

    @Test
    void handleConstraintViolation_returns400WithViolations() {
        // Given - use a real ConstraintViolationException with a mock
        ConstraintViolationException ex = new ConstraintViolationException("must not be blank", Set.of());

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void handleMissingParameter_returns400() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("city", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParameter(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("MISSING_PARAMETER");
        assertThat(response.getBody().getMessage()).contains("city");
    }

    @Test
    void handleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "days", null, null);

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("INVALID_PARAMETER");
        assertThat(response.getBody().getMessage()).contains("days");
    }

    @Test
    void handleAuthenticationException_returns401() {
        ResponseEntity<ErrorResponse> response1 = handler.handleAuthenticationException(new BadCredentialsException("Bad creds"));
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response1.getBody().getError()).isEqualTo("UNAUTHORIZED");

        ResponseEntity<ErrorResponse> response2 = handler.handleAuthenticationException(new AuthenticationCredentialsNotFoundException("No auth"));
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleAccessDenied_returns403() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(new AccessDeniedException("Denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError()).isEqualTo("FORBIDDEN");
    }

    @Test
    void handleEntityNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(new EntityNotFoundException("Not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isEqualTo("NOT_FOUND");
    }

    @Test
    void handleResourceNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(new ResourceNotFoundException("History not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("History not found");
    }

    @Test
    void handleItineraryValidation_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleItineraryValidation(new ItineraryValidationException("Invalid coordinates"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("ITINERARY_VALIDATION_ERROR");
    }

    @Test
    void handleAiServiceException_returns503() {
        ResponseEntity<ErrorResponse> response = handler.handleAiServiceException(new AiServiceException("OpenRouter down"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getError()).isEqualTo("AI_SERVICE_UNAVAILABLE");
    }

    @Test
    void handleDataIntegrityViolation_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(new DataIntegrityViolationException("Duplicate"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("DATA_CONFLICT");
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(new IllegalArgumentException("Bad arg"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("INVALID_REQUEST");
    }

    @Test
    void handleUnexpectedException_returns500() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(new RuntimeException("Boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}