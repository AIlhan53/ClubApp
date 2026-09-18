package com.example.clubappv1.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for creating a new club.
 */
@Data
public class CreateClubRequest {

    /** The name of the club to create. */
    @NotBlank(message = "club name is required")
    private String clubName;

    /** The identifier of the user to designate as the club's responsible manager. */
    @NotNull(message = "A manager is required")
    private int idResponsable;
}
