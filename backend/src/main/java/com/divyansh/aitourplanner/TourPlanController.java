package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.dto.ErrorResponse;
import com.divyansh.aitourplanner.exception.custom.AiServiceException;
import com.divyansh.aitourplanner.exception.custom.ItineraryValidationException;
import com.divyansh.aitourplanner.model.AssistantRequest;
import com.divyansh.aitourplanner.model.AssistantResponse;
import com.divyansh.aitourplanner.model.ModifyItineraryRequest;
import com.divyansh.aitourplanner.model.TourRequest;
import com.divyansh.aitourplanner.model.TourResponse;
import com.divyansh.aitourplanner.service.RateLimitService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tour")
public class TourPlanController {

    private static final Logger log = LoggerFactory.getLogger(TourPlanController.class);

    @Autowired
    private TourPlanService tourPlanService;

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("/plan")
    public ResponseEntity<?> generateTourPlan(@Valid @RequestBody TourRequest request,
                                               BindingResult bindingResult,
                                               Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return buildValidationErrorResponse(bindingResult);
        }

        String userEmail = authentication.getName();
        if (!rateLimitService.checkAiLimit(userEmail)) {
            log.warn("AI rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse("Too many AI requests. Please wait before trying again."));
        }

        try {
            TourResponse response = tourPlanService.generatePlan(
                    request.getCity(),
                    request.getDays(),
                    request.getInterests()
            );

            rateLimitService.recordAiRequest(userEmail);
            saveToHistoryIfPossible(authentication, response);

            return ResponseEntity.ok(response);

        } catch (ItineraryValidationException ex) {
            log.error("Itinerary validation failed for city={}, days={}: {}", request.getCity(), request.getDays(), ex.getMessage());
            throw ex;
        } catch (AiServiceException ex) {
            log.error("AI service error for city={}, days={}: {}", request.getCity(), request.getDays(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error generating tour plan: {}", ex.getMessage(), ex);
            throw new AiServiceException("Failed to generate itinerary. Please try again.", ex);
        }
    }

    private void saveToHistoryIfPossible(Authentication authentication, TourResponse response) {
        if (authentication == null || isFallbackResponse(response)) {
            return;
        }
        userRepository.findByEmail(authentication.getName())
                .ifPresent(user -> searchHistoryService.save(user, response));
    }

    private boolean isFallbackResponse(TourResponse response) {
        if (response == null || response.getItinerary() == null || response.getItinerary().isEmpty()) {
            return true;
        }
        return response.getItinerary().values().stream()
                .flatMap(day -> day.values().stream())
                .flatMap(List::stream)
                .allMatch(place -> "No data".equals(place.getName()) || "Error".equals(place.getName()));
    }

    @PostMapping("/modify")
    public ResponseEntity<?> modifyItinerary(@Valid @RequestBody ModifyItineraryRequest request,
                                              BindingResult bindingResult,
                                              Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return buildValidationErrorResponse(bindingResult);
        }

        String userEmail = authentication.getName();
        if (!rateLimitService.checkAiLimit(userEmail)) {
            log.warn("AI rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse("Too many AI requests. Please wait before trying again."));
        }

        try {
            TourResponse updated = tourPlanService.modifyItinerary(request);
            rateLimitService.recordAiRequest(userEmail);
            return ResponseEntity.ok(updated);

        } catch (ItineraryOperationException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error modifying itinerary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Something went wrong modifying the itinerary."));
        }
    }

    @PostMapping("/assistant")
    public ResponseEntity<?> askAssistant(@Valid @RequestBody AssistantRequest request,
                                           BindingResult bindingResult,
                                           Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return buildValidationErrorResponse(bindingResult);
        }

        String userEmail = authentication.getName();
        if (!rateLimitService.checkAiLimit(userEmail)) {
            log.warn("AI rate limit exceeded for user: {}", userEmail);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse("Too many AI requests. Please wait before trying again."));
        }

        try {
            AssistantResponse response = tourPlanService.assistantChat(request);
            rateLimitService.recordAiRequest(userEmail);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error in travel assistant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("The travel assistant is unavailable right now."));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return buildValidationErrorResponse(ex.getBindingResult());
    }

    private ResponseEntity<?> buildValidationErrorResponse(BindingResult bindingResult) {
        String errors = bindingResult.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErrorResponse(errors));
    }
}