package com.example.clubappv1.controllers;

import com.example.clubappv1.dto.LoginRequest;
import com.example.clubappv1.dto.RegisterRequest;
import com.example.clubappv1.services.AuthServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.rmi.AlreadyBoundException;
import java.util.Map;

/**
 * REST controller handling authentication-related operations.
 *
 * <p>Exposes endpoints for user login, registration, account activation
 * via email token, and logout.</p>
 */
@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    private final AuthServices authServices;

    AuthController(AuthServices authServices) {
        this.authServices = authServices;
    }

    /**
     * Authenticates a user with their email and password.
     *
     * <p>On success, returns the access and refresh tokens to be used
     * for subsequent authenticated requests.</p>
     *
     * @param request the login credentials (email and password)
     * @return a map containing the generated authentication tokens
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> tokens = authServices.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(tokens);
    }

    /**
     * Registers a new user account.
     *
     * <p>Creates the user and triggers the account activation process
     * (e.g. sending an activation email). The account is not usable
     * until it has been activated.</p>
     *
     * @param request the registration details of the new user
     * @return a confirmation message upon successful creation
     * @throws AlreadyBoundException if an account already exists for the given email
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) throws AlreadyBoundException {
        authServices.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Utilisateur créé avec succès"));
    }

    /**
     * Activates a user account using a verification token.
     *
     * <p>The token must be valid and not expired.
     * The user is redirected to the frontend login page.</p>
     *
     * @param token the activation token sent to the user by email
     * @return HTTP redirect response to the frontend login page
     */
    @GetMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam String token) {
        authServices.activateAccount(token);
        URI loginPage = URI.create("https://localhost:4200/login");
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(loginPage)
                .build();
    }

    /**
     * Logs out the current user.
     *
     * <p>Clears the security context, invalidating the current
     * authentication state.</p>
     *
     * @return logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(("Logout successfully!"));
    }
}
