package com.example.clubappv1.controllers;

import com.example.clubappv1.dto.InvitationResponse;
import com.example.clubappv1.services.TournamentInvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller handling tournament invitation consultation.
 *
 * <p>Exposes an endpoint allowing the club responsible to view the
 * invitations sent for a given tournament, along with their status
 * (pending, accepted, declined).</p>
 */
@RestController
@RequestMapping(path ="/invitation/tournaments" )
public class InvitationTournamentsController {

    private final TournamentInvitationService tournamentsInvitationServices;

    public InvitationTournamentsController(TournamentInvitationService tournamentsInvitationServices) {
        this.tournamentsInvitationServices = tournamentsInvitationServices;
    }

    /**
     * Retrieves all invitations sent for a specific tournament.
     *
     * @param id the identifier of the tournament
     * @return the list of invitations associated with the specified tournament
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<InvitationResponse>> getInvitationByTournamentId(@PathVariable int id) {
        return ResponseEntity.ok(tournamentsInvitationServices.getInvitationsByTournamentId(id));
    }
}