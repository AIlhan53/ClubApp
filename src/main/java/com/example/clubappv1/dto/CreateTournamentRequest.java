package com.example.clubappv1.dto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new tournament.
 */
@Data
public class CreateTournamentRequest {

    /** The name of the tournament to create. */
    @NotBlank(message = "A Name is required for the tournaments")
    private String tournamentName;

    /** The description of the tournament to create. */
    @NotBlank(message = "A description is required to create a tournament")
    private String tournamentDescription;

    /** The date and time at which the tournament takes place. Must be in the future. */
    @NotNull(message = "La date est requise")
    @Future(message = "La date du tournoi doit être dans le futur")
    private LocalDateTime tournamentDate;

    /** The identifier of the club organizing the tournament. */
    @NotNull(message = "Le club est requis")
    private Integer clubId;
}