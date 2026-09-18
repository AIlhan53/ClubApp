package com.example.clubappv1.dto;

import com.example.clubappv1.models.RoleType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

/**
 * Request DTO for registering a new user account.
 *
 * <p>Registering with the {@code ADMIN} role is not permitted; only
 * {@code RESPONSABLE} or {@code MEMBRE} are accepted.</p>
 */
@Data
@ToString(exclude = "password")
public class RegisterRequest {

    /** The email address of the new account. */
    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    /** The first name of the new user. */
    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    /** The last name of the new user. */
    @NotBlank(message = "Le nom est requis")
    private String lastName;

    /**
     * The password for the new account.
     *
     * <p>Must be at least 6 characters long and contain at least one
     * uppercase letter, one lowercase letter, one digit, and one special
     * character ({@code @#$%^&+=!?}).</p>
     */
    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!?]).+$",
            message = "Le mot de passe doit contenir une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;

    /** The requested global role for the new user: {@code RESPONSABLE} or {@code MEMBRE}. */
    @NotNull(message = "Le rôle est requis")
    private RoleType role; // RESPONSABLE or MEMBRE
}