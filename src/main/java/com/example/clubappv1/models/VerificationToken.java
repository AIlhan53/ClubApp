package com.example.clubappv1.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an email verification token.
 * <p>
 * This token is generated when a user registers and is used to verify
 * their email address. The token expires after a specified duration.
 * </p>
 *
 * <p>Workflow:</p>
 * <ol>
 *   <li>User registers with email</li>
 *   <li>System generates a verification token</li>
 *   <li>Email with verification link is sent to user</li>
 *   <li>User clicks link, token is validated</li>
 *   <li>If valid and not expired, user account is activated</li>
 * </ol>
 *
 * @author ClubApp Team
 * @version 1.0
 * @see Users
 */
@Setter
@Getter
@Entity
public class VerificationToken {

    /**
     * Unique identifier for the verification token.
     * -- GETTER --
     *  Gets the token ID.
     *
     *
     * -- SETTER --
     *  Sets the token ID.
     *
     @return the unique identifier
      * @param id the unique identifier

     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * The unique token string sent to the user's email.
     * <p>
     * Typically a UUID or secure random string.
     * </p>
     * -- GETTER --
     *  Gets the verification token string.
     *
     *
     * -- SETTER --
     *  Sets the verification token string.
     *
     @return the token string
      * @param token the token string

     */
    private String token;

    /**
     * The user associated with this verification token.
     * -- GETTER --
     *  Gets the user associated with this token.
     *
     *
     * -- SETTER --
     *  Sets the user associated with this token.
     *
     @return the user to be verified
      * @param user the user to be verified

     */
    @OneToOne
    private Users user;

    /**
     * The date and time when this token expires.
     * <p>
     * Tokens should not be accepted after this timestamp.
     * </p>
     * -- GETTER --
     *  Gets the expiry date of the token.
     *
     *
     * -- SETTER --
     *  Sets the expiry date of the token.
     *
     @return the expiration timestamp
      * @param expiryDate the expiration timestamp

     */
    private LocalDateTime expiryDate;

}