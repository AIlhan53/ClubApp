package com.example.clubappv1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * Request DTO for authenticating a user (login).
 */
@Data
@ToString(exclude = "password")
public class LoginRequest {

    /** The email address of the user attempting to log in. */
    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    /** The raw (unencoded) password of the user attempting to log in. */
    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}
