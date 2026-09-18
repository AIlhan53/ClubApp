package com.example.clubappv1.Exception;

import org.springframework.security.access.AccessDeniedException;
import java.rmi.AlreadyBoundException;
/**
 * Base class for the application's custom business exceptions.
 *
 * <p>Groups together the specific exception types used across the services
 * to signal authorization failures, data conflicts, and invalid tokens.
 * Each nested type extends an existing exception class so it is handled
 * appropriately by {@link GlobalExceptionHandler} (or, for
 * {@link InsufficientRoleException}, natively by Spring Security).</p>
 */
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }

    /**
     * Thrown when the current user does not have sufficient rights to
     * perform the requested action.
     *
     * <p>Extends {@link AccessDeniedException} so it integrates with
     * Spring Security's existing access-denial handling, and is mapped
     * to a {@code 403 Forbidden} response.</p>
     */
    public static class InsufficientRoleException extends AccessDeniedException {
        public InsufficientRoleException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when an operation conflicts with data that already exists
     * (e.g. a duplicate name or email, or an already-answered invitation).
     *
     * <p>Extends {@link AlreadyBoundException} and is mapped to a
     * {@code 409 Conflict} response.</p>
     */
    public static class AlreadyDataException extends AlreadyBoundException {
        public AlreadyDataException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when a token (e.g. account activation or invitation token) is
     * invalid or has expired.
     *
     * <p>Mapped to a {@code 401 Unauthorized} response.</p>
     */
    public static class InvalidTokenException extends CustomException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}