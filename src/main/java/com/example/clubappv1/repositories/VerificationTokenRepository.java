package com.example.clubappv1.repositories;

import com.example.clubappv1.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA repository for managing {@link VerificationToken} entities. [file:2]
 *
 * <p>Provides database access methods for email verification tokens,
 * including lookup by token string.</p> [file:2]
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Finds a verification token by its token value. [file:2]
     *
     * @param token the raw token string
     * @return an {@link Optional} containing the matching {@link VerificationToken},
     *         or empty if none is found
     */
    Optional<VerificationToken> findByToken(String token);
}
