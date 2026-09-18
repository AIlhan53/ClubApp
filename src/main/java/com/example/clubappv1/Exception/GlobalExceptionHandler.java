package com.example.clubappv1.Exception;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.rmi.AlreadyBoundException;
import java.util.HashMap;
import java.util.Map;
/**
 * Global exception handler translating application exceptions into
 * consistent JSON error responses with appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler{

    /**
     * Handles failed authentication attempts due to invalid credentials.
     *
     * @param ex the thrown exception
     * @return a {@code 401 Unauthorized} response with a generic error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email ou mot de passe incorrect"));
    }

    /**
     * Handles attempts to create data that conflicts with an already existing record.
     *
     * @param ex the thrown exception
     * @return a {@code 409 Conflict} response containing the exception's message
     */
    @ExceptionHandler(AlreadyBoundException.class)
    public ResponseEntity<Map<String, String>> handleGenericException(AlreadyBoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", String.valueOf(ex.getMessage())));
    }

    /**
     * Handles attempts to perform an action the current user is not authorized to perform.
     *
     * @param ex the thrown exception
     * @return a {@code 403 Forbidden} response containing the exception's message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles malformed or otherwise invalid requests.
     *
     * @param ex the thrown exception
     * @return a {@code 400 Bad Request} response containing the exception's message
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles invalid or expired tokens (e.g. account activation or invitation tokens).
     *
     * @param ex the thrown exception
     * @return a {@code 401 Unauthorized} response containing the exception's message
     */
    @ExceptionHandler(CustomException.InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(CustomException.InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles bean validation failures on request bodies (e.g. {@code @Valid} violations).
     *
     * @param ex the thrown exception, containing the field-level validation errors
     * @return a {@code 400 Bad Request} response mapping each invalid field to its error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}