package com.divyansh.aitourplanner;

import com.divyansh.aitourplanner.dto.AuthResponse;
import com.divyansh.aitourplanner.dto.ErrorResponse;
import com.divyansh.aitourplanner.dto.LoginRequest;
import com.divyansh.aitourplanner.dto.RegisterRequest;
import com.divyansh.aitourplanner.security.JwtUtil;
import com.divyansh.aitourplanner.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                       HttpServletRequest httpRequest,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return buildValidationErrorResponse(bindingResult);
        }

        String clientIp = getClientIp(httpRequest);
        if (!rateLimitService.checkRegisterLimit(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse(Instant.now(), HttpStatus.TOO_MANY_REQUESTS.value(),
                            "RATE_LIMIT_EXCEEDED", "Too many registration attempts. Please try again later.", null));
        }

        rateLimitService.recordRegisterAttempt(clientIp);

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());

            userService.registerUser(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(Instant.now(), HttpStatus.CONFLICT.value(),
                            "CONFLICT", ex.getMessage(), null));
        }

        rateLimitService.recordLoginSuccess(clientIp);

        String token = jwtUtil.generateToken(request.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, request.getEmail(), request.getUsername(), "Registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                    HttpServletRequest httpRequest,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return buildValidationErrorResponse(bindingResult);
        }

        String clientIp = getClientIp(httpRequest);
        if (!rateLimitService.checkLoginLimit(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse(Instant.now(), HttpStatus.TOO_MANY_REQUESTS.value(),
                            "RATE_LIMIT_EXCEEDED", "Too many login attempts. Please try again later.", null));
        }

        rateLimitService.recordLoginAttempt(clientIp);

        Optional<User> authenticated = userService.authenticate(request.getEmail(), request.getPassword());

        if (authenticated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(Instant.now(), HttpStatus.UNAUTHORIZED.value(),
                            "UNAUTHORIZED", "Invalid email or password.", null));
        }

        rateLimitService.recordLoginSuccess(clientIp);

        User loggedIn = authenticated.get();
        String token = jwtUtil.generateToken(loggedIn.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, loggedIn.getEmail(), loggedIn.getUsername(), "Login successful!"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR", errors, null));
    }

    private ResponseEntity<ErrorResponse> buildValidationErrorResponse(BindingResult bindingResult) {
        String errors = bindingResult.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR", errors, null));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        String xrHeader = request.getHeader("X-Real-IP");
        if (xrHeader != null && !xrHeader.isEmpty()) {
            return xrHeader;
        }
        return request.getRemoteAddr();
    }
}