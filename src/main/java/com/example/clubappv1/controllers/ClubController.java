package com.example.clubappv1.controllers;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.*;
import com.example.clubappv1.services.ClubServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * REST controller handling club-related operations.
 *
 * <p>Exposes endpoints to list, retrieve, create, update, and delete clubs,
 * as well as to manage club membership (joining, leaving) and consult
 * clubs from the perspective of a responsible manager or a member.</p>
 */
@RestController
@RequestMapping(path = "/club")
public class ClubController {

    private final ClubServices clubServices;

    public ClubController(ClubServices clubServices) {
        this.clubServices = clubServices;
    }

    /**
     * Retrieves all existing clubs.
     *
     * @return the list of all clubs
     */
    @GetMapping
    public ResponseEntity<List<ClubResponse>> getAllClubs() {
        return ResponseEntity.ok(clubServices.getAllClub());
    }

    /**
     * Retrieves all stores managed by a specific user (manager).
     *
     * <p>Accessible by: MANAGER, ADMIN</p>
     *
     * @return List of stores managed by the specified user
     */
    @GetMapping("/responsableClubs")
    public ResponseEntity<List<ClubResponse>> getResponsableClubs() {
        return ResponseEntity.ok(clubServices.getClubsByResponsable());
    }

    /**
     * Retrieves all clubs the current user is a member of.
     *
     * @return the list of clubs the authenticated user belongs to
     */
    @GetMapping("/memberClubs")
    public ResponseEntity<List<ClubResponse>> getMemberClubs() {
        return ResponseEntity.ok(clubServices.getAllClubsByMember());
    }

    /**
     * Retrieves all members belonging to a specific club.
     *
     * @param id the identifier of the club
     * @return the list of members of the specified club
     */
    @GetMapping("/allMember/{id}")
    public ResponseEntity<List<ClubMemberResponse>> getAllMemberOfClub(@PathVariable int id) {
        return ResponseEntity.ok(clubServices.getAllUserByClubId(id));
    }

    /**
     * Retrieves a single club by its identifier.
     *
     * @param id the identifier of the club to retrieve
     * @return the requested club
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<ClubResponse> getClubById(@PathVariable int id) {
        return ResponseEntity.ok(clubServices.findClubById(id));
    }

    /**
     * Creates a new club.
     *
     * @param request the details of the club to create
     * @return the newly created club
     * @throws CustomException.AlreadyDataException if a club with the same name already exists
     */
    @PostMapping("/create")
    public ResponseEntity<ClubResponse> createClub(@Valid @RequestBody CreateClubRequest request) throws CustomException.AlreadyDataException {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubServices.createClub(request));
    }

    /**
     * Updates an existing club.
     *
     * @param id      the identifier of the club to update
     * @param request the updated club details
     * @return the updated club
     * @throws CustomException.AlreadyDataException if the update conflicts with existing club data
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ClubResponse> updateClub(@PathVariable int id, @Valid @RequestBody UpdateClubRequest request) throws CustomException.AlreadyDataException {
        ClubResponse clubResponse = clubServices.updateClub(id,request);
        return ResponseEntity.ok(clubResponse);
    }

    /**
     * Deletes an existing club.
     *
     * @param id the identifier of the club to delete
     * @return an empty response with no content status
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable int id){
        clubServices.removeClub(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Allows the current user to join a club.
     *
     * @param id the identifier of the club to join
     * @return an empty response confirming the join
     * @throws CustomException.AlreadyDataException if the user is already a member of the club
     */
    @PostMapping("/joinClub/{id}")
    public ResponseEntity<Void> joinClub(@PathVariable int id) throws CustomException.AlreadyDataException {
        clubServices.joinClub(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Allows the current user to leave a club.
     *
     * @param id the identifier of the club to leave
     * @return an empty response confirming the departure
     */
    @PostMapping("/leaveClub/{id}")
    public ResponseEntity<Void> leaveClub(@PathVariable int id){
        clubServices.leaveClub(id);
        return ResponseEntity.ok().build();
    }
}
