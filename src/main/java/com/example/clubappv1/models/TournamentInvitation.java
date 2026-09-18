package com.example.clubappv1.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing an invitation sent to a club member to join a
 * specific tournament.
 *
 * <p>Each invitation targets exactly one {@link Users} for exactly one
 * {@link Tournaments}, and is uniquely identified by a {@code token} used
 * to accept or decline it without requiring an active session. Its
 * {@link InvitationStatus} tracks the invitation's lifecycle (pending,
 * accepted, declined, or cancelled).</p>
 */
@Entity
@Table(name = "tournament_invitations")
@Getter
@Setter
public class TournamentInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String token;


    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournaments tournament;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;
}