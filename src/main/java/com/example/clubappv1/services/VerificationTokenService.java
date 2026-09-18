package com.example.clubappv1.services;

import com.example.clubappv1.models.Users;
import com.example.clubappv1.models.VerificationToken;
import com.example.clubappv1.repositories.VerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for managing verification tokens.
 *
 * <p>
 * Verification tokens are mainly used for account activation
 * and sensitive actions such as email change confirmation.
 * </p>
 *
 * Each token:
 * <ul>
 *     <li>Is linked to a specific user</li>
 *     <li>Has a unique random value</li>
 *     <li>Has an expiration date</li>
 * </ul>
 */
@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepo;

    /**
     * Constructs the VerificationTokenService.
     *
     * @param tokenRepo repository used to persist verification tokens
     */
    public VerificationTokenService(VerificationTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    /**
     * Creates and persists a new verification token for a user.
     *
     * The generated token:
     * <ul>
     *     <li>Uses a random UUID</li>
     *     <li>Expires after 24 hours</li>
     * </ul>
     *
     * @param user the user for whom the token is created
     * @return the persisted {@link VerificationToken}
     */
    public VerificationToken createTokenForUser(Users user) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        return tokenRepo.save(token);
    }

    /**
     * Retrieves a verification token by its token value.
     *
     * @param token the token string
     * @return an {@link Optional} containing the token if found, otherwise empty
     */
    public Optional<VerificationToken> getByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    /**
     * Deletes a verification token.
     *
     * <p>
     * This is typically used after successful token validation
     * to prevent reuse.
     * </p>
     *
     * @param token the verification token to delete
     */
    public void deleteToken(VerificationToken token) {
        tokenRepo.delete(token);
    }
}
