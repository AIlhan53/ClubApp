package com.example.clubappv1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO representing a club member, exposed to API clients.
 *
 * <p>Used for the club's member listing, where only the member's name
 * needs to be displayed to the club's responsible manager.</p>
 */
@Data
@AllArgsConstructor
public class ClubMemberResponse {

    /** The member's first name. */
    private String firstName;

    /** The member's last name. */
    private String lastName;
}