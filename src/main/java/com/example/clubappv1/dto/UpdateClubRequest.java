package com.example.clubappv1.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for updating an existing club.
 */
@Data
public class UpdateClubRequest {

    /** The club's new name. */
    @NotBlank(message = "club name is required")
    private String clubName;

    /** Whether the club should be marked as active. */
    private boolean active;
}
