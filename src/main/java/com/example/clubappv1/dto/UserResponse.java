package com.example.clubappv1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO representing a user, exposed to API clients.
 *
 * <p>Excludes sensitive fields such as the password.</p>
 */
@Data
@AllArgsConstructor
public class UserResponse {

    /** The user's identifier. */
    private int id;

    /** The user's email address. */
    private String email;

    /** The user's first name. */
    private String firstName;

    /** The user's last name. */
    private String lastName;

    /** Whether the user's account is active. */
    private boolean active;

    /** The name of the user's global role (e.g. MEMBRE, RESPONSABLE, ADMIN). */
    private String roleName;
}