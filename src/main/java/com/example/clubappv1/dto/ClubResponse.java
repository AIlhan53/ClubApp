package com.example.clubappv1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * Response DTO representing a club, exposed to API clients.
 *
 * <p>Includes, from the requesting user's own perspective, whether they
 * are a member ({@code joined}) and/or the club's responsible manager
 * ({@code responsible}).</p>
 */
@Data
@AllArgsConstructor
public class ClubResponse {

    /** The club's identifier. */
    private int id;

    /** The club's name. */
    private String clubName;

    /** The first name of the club's responsible manager. */
    private String responsableName;

    /** Whether the club is active. */
    private boolean active;

    /** Whether the requesting user is a member of this club. */
    private boolean joined;

    /** Whether the requesting user is this club's responsible manager. */
    private boolean responsible;

    /** The number of members in this club. */
    private int memberCount;

    /** The number of tournaments organized by this club. */
    private int tournamentCount;
}