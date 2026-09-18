package com.example.clubappv1.controllers;


import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.CreateTournamentRequest;
import com.example.clubappv1.dto.TournamentResponse;
import com.example.clubappv1.services.TournamentsServices;
import com.example.clubappv1.services.UsersServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller handling tournament-related operations.
 *
 * <p>Exposes endpoints to list, retrieve, and create tournaments, respond to
 * tournament invitations (accept/decline), leave a tournament, and delete
 * an existing tournament.</p>
 */
@RestController
@RequestMapping(path ="/tournaments" )
public class TournamentsController {

    private final TournamentsServices tournamentsServices;

    private final String frontendUrl ="https://localhost:4200";

    public TournamentsController(TournamentsServices tournamentsServices) {
        this.tournamentsServices = tournamentsServices;
    }

    /**
     * Retrieves all existing tournaments.
     *
     * @return the list of all tournaments
     */
    @GetMapping
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        return ResponseEntity.ok(tournamentsServices.getAllTournaments());
    }

    /**
     * Retrieves a single tournament by its identifier.
     *
     * @param id the identifier of the tournament to retrieve
     * @return the requested tournament
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable int id) {
        return ResponseEntity.ok(tournamentsServices.getTournamentById(id));
    }

    /**
     * Retrieves all tournaments organized by a specific club.
     *
     * @param id the identifier of the club
     * @return the list of tournaments organized by the specified club
     */
    @GetMapping("/allTournamentByClub/{id}")
    public ResponseEntity<List<TournamentResponse>> getAllTournamentOfClub(@PathVariable int id) {
        return ResponseEntity.ok(tournamentsServices.getAllTournamentByClubId(id));
    }

    /**
     * Creates a new tournament.
     *
     * <p>Automatically sends invitations to the members of the organizing club.</p>
     *
     * @param request the details of the tournament to create
     * @return the newly created tournament
     * @throws CustomException.AlreadyDataException if a tournament with the same name already exists
     */
    @PostMapping("/create")
    public ResponseEntity<TournamentResponse> createTournament(@Valid @RequestBody CreateTournamentRequest request) throws CustomException.AlreadyDataException {
        return ResponseEntity.status(HttpStatus.CREATED).body(tournamentsServices.createTournaments(request));
    }

    /**
     * Accepts a tournament invitation using its token.
     *
     * <p>Identifies the invited user directly through the token, so no
     * active session is required to respond to the invitation.</p>
     *
     * @param id    the identifier of the tournament
     * @param token the invitation token sent to the user by email
     * @return an empty response confirming the acceptance
     * @throws CustomException.AlreadyDataException if the invitation has already been answered
     *         or the user is already registered for the tournament
     */
    @GetMapping("/invitation/{id}/accept")
    public ResponseEntity<Void> joinTournament(
            @PathVariable int id,
            @RequestParam String token
    ) throws CustomException.AlreadyDataException {

        tournamentsServices.acceptInvitation(id, token);

        URI redirectUri = URI.create(frontendUrl + "/invitation-result?status=accepted"
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .build();
    }

    /**
     * Declines a tournament invitation using its token.
     *
     * <p>Identifies the invited user directly through the token, so no
     * active session is required to respond to the invitation.</p>
     *
     * @param id    the identifier of the tournament
     * @param token the invitation token sent to the user by email
     * @return an empty response confirming the decline
     * @throws CustomException.AlreadyDataException if the invitation has already been answered
     */
    @GetMapping("/invitation/{id}/decline")
    public ResponseEntity<Void> declineTournament(
            @PathVariable int id,
            @RequestParam String token
    ) throws CustomException.AlreadyDataException {

        tournamentsServices.declineInvitationTournaments(id, token);

        URI redirectUri = URI.create(frontendUrl + "/invitation-result?status=declined");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .build();
    }

    /**
     * Allows the current user to leave a tournament they had joined.
     *
     * @param id the identifier of the tournament to leave
     * @return an empty response confirming the departure
     */
    @GetMapping("/quitTournament/{id}")
    public ResponseEntity<Void> quitTournament(@PathVariable int id){
        tournamentsServices.quitTournaments(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes an existing tournament.
     *
     * @param id the identifier of the tournament to delete
     * @return an empty response with no content status
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable int id){
        tournamentsServices.removeTournament(id);
        return ResponseEntity.noContent().build();
    }
}
