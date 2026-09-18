package com.example.clubappv1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO representing a tournament, exposed to API clients.
 *
 * <p>Includes whether the requesting user is already registered
 * ({@code joined}), from their own perspective.</p>
 */
@Data
@AllArgsConstructor
public class TournamentResponse {

    /** The tournament's identifier. */
    private int id;

    /** The tournament's name. */
    private String tournamentName;

    /** The tournament's description. */
    private String tournamentDescription;

    /** The date and time at which the tournament takes place. */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime tournamentDate;

    /** The name of the club organizing this tournament. */
    private String clubName;

    /** Whether the requesting user is already registered for this tournament. */
    private boolean joined;

    /** The number of users currently registered for this tournament. */
    private int playerCount;
}