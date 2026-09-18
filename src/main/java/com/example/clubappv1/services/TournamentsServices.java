package com.example.clubappv1.services;


import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.CreateTournamentRequest;
import com.example.clubappv1.dto.TournamentResponse;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.ClubsRepository;
import com.example.clubappv1.repositories.InvitationRepository;
import com.example.clubappv1.repositories.TournamentsRepository;
import com.example.clubappv1.repositories.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service handling tournament-related business logic.
 *
 * <p>Provides operations to list, retrieve, create, and delete tournaments,
 * respond to tournament invitations via token (accept/decline), and allow
 * a user to leave a tournament they had joined.</p>
 */
@Service
public class TournamentsServices {

    private final TournamentsRepository tournamentsRepository;
    private final ClubsRepository clubsRepository;
    private final CurrentUserService currentUserService;
    private final TournamentInvitationService tournamentInvitationService;
    private final InvitationRepository invitationRepository;
    private final UsersRepository  usersRepository;

    public TournamentsServices(TournamentsRepository tournamentsRepository,
                               ClubsRepository clubsRepository,
                               CurrentUserService currentUserService,
                               TournamentInvitationService tournamentInvitationService,
                               InvitationRepository invitationRepository,
                               UsersRepository usersRepository) {
        this.tournamentsRepository = tournamentsRepository;
        this.clubsRepository = clubsRepository;
        this.currentUserService = currentUserService;
        this.tournamentInvitationService = tournamentInvitationService;
        this.invitationRepository = invitationRepository;
        this.usersRepository = usersRepository;
    }

    /**
     * Retrieves all existing tournaments.
     *
     * <p>For each tournament, indicates whether the current user is
     * already registered.</p>
     *
     * @return the list of all tournaments, from the perspective of the current user
     */
    public List<TournamentResponse> getAllTournaments() {

        UserResponse currentUser = currentUserService.getCurrentUser();

        return tournamentsRepository.findAll().stream()
                .map(t -> new TournamentResponse(
                        t.getId(),
                        t.getTournamentName(),
                        t.getTournamentDescription(),
                        t.getTournamentDate(),
                        t.getClub().getName(),
                        t.getUsers().stream()
                                .anyMatch(u -> u.getId() == currentUser.getId()),
                        t.getUsers().size()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single tournament by its identifier.
     *
     * @param id the identifier of the tournament to retrieve
     * @return the requested tournament, from the perspective of the current user
     * @throws NoSuchElementException if no tournament matches the given id
     */
    public TournamentResponse getTournamentById(int id) {

        UserResponse currentUser = currentUserService.getCurrentUser();


        Tournaments t = tournamentsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tournament not found"));

        return new TournamentResponse(
                t.getId(),
                t.getTournamentName(),
                t.getTournamentDescription(),
                t.getTournamentDate(),
                t.getClub().getName(),
                t.getUsers().stream()
                        .anyMatch(u -> u.getId() == currentUser.getId()),
                t.getUsers().size());
    }

    /**
     * Retrieves all tournaments organized by a specific club.
     *
     * @param clubId the identifier of the club
     * @return the list of tournaments organized by the specified club,
     *         from the perspective of the current user
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no club matches the given id
     */
    public List<TournamentResponse> getAllTournamentByClubId(int clubId) {

        UserResponse currentUser = currentUserService.getCurrentUser();

        checkIfIdPositive(clubId);

        Clubs club = clubsRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("Club not found"));

        List<Tournaments> tournaments =
                tournamentsRepository.findAllTournamentsByClub_id(clubId);

        return tournaments.stream()
                .map(tournament -> new TournamentResponse(
                        tournament.getId(),
                        tournament.getTournamentName(),
                        tournament.getTournamentDescription(),
                        tournament.getTournamentDate(),
                        tournament.getClub().getName(),
                        tournament.getUsers().stream()
                                .anyMatch(user -> user.getId() == currentUser.getId()),
                        tournament.getUsers().size()
                ))
                .toList();
    }

    /**
     * Accepts a tournament invitation using its token.
     *
     * <p>Identifies the invited user directly through the invitation, so
     * no active session is required to respond. The invited user must be
     * a member of the tournament's organizing club, must not already be
     * registered for the tournament, and the invitation must still be
     * pending.</p>
     *
     * @param tournamentId the identifier of the tournament
     * @param token        the invitation token sent to the user by email
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no invitation matches the given token, or if the
     *         invitation does not belong to the specified tournament
     * @throws CustomException.AlreadyDataException if the invitation has already been answered,
     *         or the invited user is already registered for the tournament
     * @throws CustomException.InsufficientRoleException if the invited user is not a member
     *         of the tournament's organizing club
     */
    @Transactional
    public void acceptInvitation(int tournamentId, String token) throws CustomException.AlreadyDataException {
        checkIfIdPositive(tournamentId);

        TournamentInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invitation not found"));

        Tournaments tournament = invitation.getTournament();

        if (tournament.getId() != tournamentId) {
            throw new NoSuchElementException("Invitation does not match this tournament");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new CustomException.AlreadyDataException("This invitation has already been answered");
        }

        Users invitedUser = invitation.getUser();

        if (!invitedUser.isActive()) {
            throw new CustomException.InsufficientRoleException("You cannot join tournament if you are deactivated");
        }

        if (!tournament.getClub().getUsers().contains(invitedUser)) {
            throw new CustomException.InsufficientRoleException("You must be a member of the organizing club to join this tournament");
        }

        if (tournament.getUsers().contains(invitedUser)) {
            throw new CustomException.AlreadyDataException("You are already registered for this tournament");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);

        tournament.getUsers().add(invitedUser);
        invitedUser.getTournaments().add(tournament);
    }

    /**
     * Declines a tournament invitation using its token.
     *
     * <p>Identifies the invited user directly through the invitation, so
     * no active session is required to respond.</p>
     *
     * @param tournamentId the identifier of the tournament
     * @param token        the invitation token sent to the user by email
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no invitation matches the given token, or if the
     *         invitation does not belong to the specified tournament
     * @throws CustomException.AlreadyDataException if the invitation has already been answered
     */
    @Transactional
    public void declineInvitationTournaments(int tournamentId, String token) throws CustomException.AlreadyDataException {
        checkIfIdPositive(tournamentId);

        TournamentInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invitation not found"));

        Users invitedUser = invitation.getUser();
        if (!invitedUser.isActive()) {
            throw new CustomException.InsufficientRoleException("You cannot join tournament if you are deactivated");
        }

        if (invitation.getTournament().getId() != tournamentId) {
            throw new NoSuchElementException("Invitation does not match this tournament");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new CustomException.AlreadyDataException("This invitation has already been answered");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
    }

    /**
     * Allows the current user to leave a tournament they had joined.
     *
     * <p>If the current user is the tournament's organizing club responsible,
     * they are simply removed from the tournament's participants. Otherwise,
     * any related pending invitation is marked as cancelled before the user
     * is removed from the tournament.</p>
     *
     * @param tournamentId the identifier of the tournament to leave
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no tournament matches the given id, or if the
     *         current user is not participating in the tournament
     */
    @Transactional
    public void quitTournaments(int tournamentId) {
        checkIfIdPositive(tournamentId);

        Users currentUser = currentUserService.getCurrentUserEntity();

        Tournaments tournament = tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new NoSuchElementException("Tournament not found"));

        if (!tournament.getUsers().contains(currentUser)) {
            throw new NoSuchElementException("You are not participating to this tournament");
        }

        if (currentUser.equals(tournament.getClub().getResponsable())) {
            tournament.getUsers().removeIf(u -> u.getId() == currentUser.getId());
            currentUser.getTournaments().removeIf(c -> c.getId() == tournamentId);
        }
        else{
            invitationRepository
                    .findByTournament_idAndUser_id(tournament.getId(), currentUser.getId())
                    .ifPresent(invitation -> invitation.setStatus(InvitationStatus.CANCELLED));

            tournament.getUsers().removeIf(u -> u.getId() == currentUser.getId());
            currentUser.getTournaments().removeIf(c -> c.getId() == tournamentId);
        }
    }

    /**
     * Creates a new tournament for a club.
     *
     * <p>The current user must be either the club's responsible manager or
     * an admin. The creator is automatically registered as a participant,
     * and invitations are sent to the other members of the organizing club.</p>
     *
     * @param request the details of the tournament to create
     * @return the newly created tournament
     * @throws NoSuchElementException if no club matches the given club id
     * @throws CustomException.AlreadyDataException if a tournament with the same name already exists
     * @throws CustomException.InsufficientRoleException if the current user is neither the
     *         club's responsible manager nor an admin
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Transactional
    public TournamentResponse createTournaments(CreateTournamentRequest request) throws CustomException.AlreadyDataException {

        Users currentUser = currentUserService.getCurrentUserEntity();

        Clubs club = clubsRepository.findById(request.getClubId())
                .orElseThrow(() -> new NoSuchElementException("Club not found"));

        checkCanCreateTournament(currentUser, club);

        String tournamentName = validateTournamentName(request.getTournamentName());

        Tournaments tournament = buildTournament(request, club, tournamentName);

        //Inscrire le responsable dans son propre tournoi
        tournament.getUsers().add(currentUser);
        currentUser.getTournaments().add(tournament);

        tournamentsRepository.save(tournament);

        tournamentInvitationService.sendTournamentInvitations(tournament, club, currentUser);

        return new TournamentResponse(
                tournament.getId(),
                tournament.getTournamentName(),
                tournament.getTournamentDescription(),
                tournament.getTournamentDate(),
                tournament.getClub().getName(),
                tournament.getUsers().contains(currentUser),
                tournament.getUsers().size());
    }

    /**
     * Deletes an existing tournament.
     *
     * <p>Cascades the deletion by removing all related invitations and
     * participations before deleting the tournament itself. Only the
     * responsible manager of the tournament's organizing club is allowed
     * to perform this action.</p>
     *
     * @param id the identifier of the tournament to delete
     * @throws AccessDeniedException if declared but not thrown in the current implementation
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no tournament matches the given id, or if the
     *         tournament has no associated club
     * @throws CustomException.InsufficientRoleException if the current user is not the
     *         responsible manager of the tournament's organizing club
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Transactional
    public void removeTournament(int id) throws AccessDeniedException {

        checkIfIdPositive(id);

        Users currentUser = currentUserService.getCurrentUserEntity();

        Tournaments tournament = tournamentsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tournament not found"));

        Clubs club = tournament.getClub();

        if(club == null){
            throw new NoSuchElementException("Club not found");
        }

        if (!Objects.equals(currentUser.getId(), club.getResponsable().getId())) {
            throw new CustomException.InsufficientRoleException("You are not allowed to remove this tournament");
        }

        invitationRepository.deleteAll(invitationRepository.findAllByTournament_id(id));

        List<Users> participants = new ArrayList<>(tournament.getUsers());

        for (Users user : participants) {
            user.getTournaments().remove(tournament);
        }

        tournament.getUsers().clear();

        usersRepository.saveAll(participants);
        usersRepository.flush();

        tournamentsRepository.delete(tournament);
    }

    /**
     * Ensures a user is allowed to create a tournament for the given club,
     * i.e. is either the club's responsible manager or an admin.
     *
     * @param currentUser the user attempting to create the tournament
     * @param club        the club the tournament would be organized by
     * @throws CustomException.InsufficientRoleException if the user is neither the club's
     *         responsible manager nor an admin
     */
    private void checkCanCreateTournament(Users currentUser, Clubs club) {
        boolean isResponsible = Objects.equals(currentUser.getId(), club.getResponsable().getId());

        if (!isResponsible) {
            throw new CustomException.InsufficientRoleException("You are not allowed to create a tournament in this club");
        }
    }

    /**
     * Validates and normalizes a tournament name, ensuring it is not already in use.
     *
     * @param tournamentName the raw tournament name to validate
     * @return the trimmed tournament name
     * @throws CustomException.AlreadyDataException if a tournament with the same name already exists
     */
    private String validateTournamentName(String tournamentName) throws CustomException.AlreadyDataException {

        String name = tournamentName.trim();

        if (tournamentsRepository.findByTournamentNameIgnoreCase(name).isPresent()) {
            throw new CustomException.AlreadyDataException("This tournament name is unavailable");
        }
        return name;
    }

    /**
     * Builds a new {@link Tournaments} entity from a creation request.
     *
     * @param request        the tournament creation details
     * @param club           the club organizing the tournament
     * @param tournamentName the validated, trimmed tournament name
     * @return a new, unsaved tournament entity, marked as active
     */
    private Tournaments buildTournament(CreateTournamentRequest request, Clubs club, String tournamentName) {

        Tournaments tournament = new Tournaments();
        tournament.setTournamentDate(request.getTournamentDate());
        tournament.setClub(club);
        tournament.setTournamentName(tournamentName);
        tournament.setTournamentDescription(request.getTournamentDescription().trim());

        tournament.setActive(true);

        return tournament;
    }

    /**
     * Ensures a given identifier is strictly positive.
     *
     * @param id the identifier to validate
     * @throws IllegalArgumentException if the given id is not positive
     */
    private void checkIfIdPositive(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }
    }
}