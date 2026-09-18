package com.example.clubappv1.dto;


import com.example.clubappv1.models.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * Request DTO for updating an existing user's profile.
 *
 * <p>All fields are optional: only non-null/non-blank fields are applied
 * by {@code UsersServices.updateUsers}. The password can only be changed
 * by the user themselves, and the role can only be changed by an admin.</p>
 */
@Data
@ToString(exclude = "password")
public class UpdateUserRequest {

    /** The user's new email address, if it should be changed. */
    @Email(message = "Format d'email invalide")
    private String email;

    /** The user's new first name, if it should be changed. */
    private String firstName;

    /** The user's new last name, if it should be changed. */
    private String lastName;

    /**
     * The user's new password, if it should be changed.
     *
     * <p>Must be at least 6 characters long and contain at least one
     * uppercase letter, one lowercase letter, one digit, and one special
     * character ({@code @#$%^&+=!?}). Only the user themselves is allowed
     * to change their own password.</p>
     */
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!?]).+$",
            message = "Le mot de passe doit contenir une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;

    /** The user's new global role, if it should be changed. Only an admin may set this. */
    private RoleType role;  //seul un admin peut le changer
}