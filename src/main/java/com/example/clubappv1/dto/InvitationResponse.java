package com.example.clubappv1.dto;


import com.example.clubappv1.models.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO representing a tournament invitation, exposed to API clients.
 *
 * <p>Combines information about the tournament being invited to and the
 * invited member, along with the invitation's current status.</p>
 */
@Data
@AllArgsConstructor
public class InvitationResponse {

    /** The invitation's identifier. */
    private int id;

    /** The name of the tournament the invitation is for. */
    private String tournamentName;

    /** The description of the tournament the invitation is for. */
    private String tournamentDescription;

    /** The first name of the invited member. */
    private String firstNameMember;

    /** The last name of the invited member. */
    private String lastNameMember;

    /** The current status of the invitation (pending, accepted, declined, or cancelled). */
    private InvitationStatus invitationStatus;
}