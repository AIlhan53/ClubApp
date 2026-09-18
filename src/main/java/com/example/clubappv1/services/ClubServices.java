package com.example.clubappv1.services;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.*;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Service handling club-related business logic.
 *
 * <p>Provides operations to list, retrieve, create, update, and delete
 * clubs, manage club membership (joining, leaving), consult club members
 * and tournaments, and view clubs from the perspective of the current
 * user, either as a member or as the club's responsible manager.</p>
 */
@Service
public class ClubServices {

    private final ClubsRepository clubsRepository;
    private final UsersRepository usersRepository;
    private final TournamentsRepository tournamentsRepository;
    private final CurrentUserService currentUserService;
    private final InvitationRepository invitationRepository;

    public ClubServices(ClubsRepository clubsRepository,
                        UsersRepository usersRepository,
                        TournamentsRepository tournamentsRepository,
                        CurrentUserService currentUserService,
                        InvitationRepository invitationRepository) {
        this.clubsRepository = clubsRepository;
        this.usersRepository = usersRepository;
        this.tournamentsRepository = tournamentsRepository;
        this.currentUserService = currentUserService;
        this.invitationRepository = invitationRepository;
    }

    /**
     * Retrieves all existing clubs.
     *
     * <p>For each club, indicates whether the current user is a member
     * and/or the responsible manager.</p>
     *
     * @return the list of all clubs, from the perspective of the current user
     */
    public List<ClubResponse> getAllClub() {
        Users currentUser = currentUserService.getCurrentUserEntity();

        List<Clubs> clubs = clubsRepository.findAll();

        return clubs.stream()
                .map(club -> {
                    boolean joined = club.getUsers().stream()
                            .anyMatch(user -> Objects.equals(user.getId(), currentUser.getId())
                            );

                    boolean responsible = club.getResponsable() != null
                            && Objects.equals(club.getResponsable().getId(), currentUser.getId()
                    );
                    return new ClubResponse(
                            club.getId(),
                            club.getName(),
                            club.getResponsable().getFirstName(),
                            club.isActive(),
                            joined,
                            responsible,
                            club.getUsers().size(),
                            club.getTournaments().size()
                    );
                })
                .toList();
    }

    /**
     * Retrieves a single club by its identifier.
     *
     * @param id the identifier of the club to retrieve
     * @return the requested club, from the perspective of the current user
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no club matches the given id
     */
    public ClubResponse findClubById(int id) {
        checkIfIdPositive(id);

        Users currentUser = currentUserService.getCurrentUserEntity();

        Clubs club = clubsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Club not found"));

        boolean responsible = club.getResponsable() != null
                && Objects.equals(club.getResponsable().getId(), currentUser.getId()
        );

        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getResponsable().getFirstName(),
                club.isActive(),
                club.getUsers().contains(currentUser),
                responsible,
                club.getUsers().size(),
                club.getTournaments().size()
        );
    }

    /**
     * Retrieves a club entity by its exact name.
     *
     * @param name the name of the club to retrieve
     * @return the matching club entity
     * @throws NoSuchElementException if no club matches the given name
     */
    public Clubs findClubByName(String name) {
        return clubsRepository.findClubsByName(name)
                .orElseThrow(() -> new NoSuchElementException("Club not found"));
    }

    /**
     * Retrieves all stores managed by a specific user (manager).
     *
     * @return list of stores owned by the manager
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    public List<ClubResponse> getClubsByResponsable() {

        Users currentUser = currentUserService.getCurrentUserEntity();

        List<Clubs> clubs = clubsRepository.findAllClubsByResponsable(currentUser);

        return clubs.stream()
                .map(club -> {

                    boolean joined = club.getUsers().stream()
                            .anyMatch(user -> Objects.equals(user.getId(), currentUser.getId())
                            );

                    boolean responsible = club.getResponsable() != null
                            && Objects.equals(club.getResponsable().getId(), currentUser.getId()
                    );

                    return new ClubResponse(
                            club.getId(),
                            club.getName(),
                            club.getResponsable().getFirstName(),
                            club.isActive(),
                            joined,
                            responsible,
                            club.getUsers().size(),
                            club.getTournaments().size()
                    );
                })
                .toList();
    }

    /**
     * Retrieves the members of a specific club.
     *
     * <p>Only the club's responsible manager or an admin is allowed to
     * consult this information.</p>
     *
     * @param clubId the identifier of the club
     * @return the list of members of the specified club
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no club matches the given id
     * @throws CustomException.InsufficientRoleException if the current user is neither
     *         the club's responsible manager nor an admin
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    public List<ClubMemberResponse> getAllUserByClubId(int clubId) {

        checkIfIdPositive(clubId);

        Users currentUser = currentUserService.getCurrentUserEntity();

        boolean isAdmin = currentUser.getRole().getName() == RoleType.ADMIN;

        Clubs club = clubsRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("Club not found"));

        // Vérifier que le responsable gère bien ce club ou que c'est un admin
        if (club.getResponsable().getId() != currentUser.getId() && !isAdmin) {
            throw new CustomException.InsufficientRoleException("You are not allowed to execute this operation");
        }

        List<Users> users = usersRepository.findAllUsersByClubs_id(clubId);

        return users.stream()
                .map(user -> new ClubMemberResponse(
                        user.getFirstName(),
                        user.getLastName()
                ))
                .toList();
    }

    /**
     * Retrieves all clubs the current user is a member of.
     *
     * @return the list of clubs the current user belongs to
     */
    public List<ClubResponse> getAllClubsByMember() {

        Users currentUser = currentUserService.getCurrentUserEntity();

        List<Clubs> clubs = clubsRepository.findAllClubsByUsers(currentUser);

        return clubs.stream()
                .map(club -> {

                    boolean joined = club.getUsers().stream()
                            .anyMatch(user -> Objects.equals(user.getId(), currentUser.getId())
                            );

                    boolean responsible = club.getResponsable() != null
                            && Objects.equals(club.getResponsable().getId(), currentUser.getId()
                    );

                    return new ClubResponse(
                            club.getId(),
                            club.getName(),
                            club.getResponsable().getFirstName(),
                            club.isActive(),
                            joined,
                            responsible,
                            club.getUsers().size(),
                            club.getTournaments().size()
                    );
                })
                .toList();
    }

    /**
     * Creates a new club.
     *
     * <p>The current user becomes both the responsible manager and the
     * first member of the club. Members are not allowed to create clubs.</p>
     *
     * @param request the details of the club to create
     * @return the newly created club
     * @throws AccessDeniedException if declared but not thrown in the current implementation
     * @throws CustomException.AlreadyDataException if a club with the same name already exists
     * @throws CustomException.InsufficientRoleException if the current user has the MEMBRE role
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Transactional
    public ClubResponse createClub(CreateClubRequest request) throws CustomException.AlreadyDataException {

        Users currentUser = currentUserService.getCurrentUserEntity();

        checkIfMemberNotAuthorized(currentUser);

        String clubName = request.getClubName().trim();

        // Pour éviter la récurrence et les espaces inutiles
        if (clubsRepository.existsByNameIgnoreCase(clubName)) {
            throw new CustomException.AlreadyDataException("This club name is unavailable");
        }

        Clubs club = new Clubs();
        club.setName(clubName);
        club.setResponsable(currentUser);
        club.setActive(true);

        club.getUsers().add(currentUser);
        currentUser.getClubs().add(club);

        clubsRepository.save(club);

        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getResponsable().getFirstName(),
                club.isActive(),
                true,
                true,
                club.getUsers().size(),
                club.getTournaments().size()
        );
    }

    /**
     * Updates an existing club.
     *
     * <p>Only the club's current responsible manager is allowed to update it.</p>
     *
     * @param clubId  the identifier of the club to update
     * @param request the updated club details
     * @return the updated club
     * @throws AccessDeniedException if declared but not thrown in the current implementation
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no club matches the given id
     * @throws CustomException.AlreadyDataException if the new name is already used by another club
     * @throws CustomException.InsufficientRoleException if the current user is not the club's responsible manager
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    public ClubResponse updateClub(int clubId, UpdateClubRequest request) throws CustomException.AlreadyDataException {

        checkIfIdPositive(clubId);

        UserResponse currentUser = currentUserService.getCurrentUser();

        Clubs clubToUpdate = clubsRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("Not found"));

        if (!Objects.equals(currentUser.getId(), clubToUpdate.getResponsable().getId())) {
            throw new CustomException.InsufficientRoleException("You are not allowed to execute this action");
        }

        String clubName = request.getClubName().trim();

        // Pour éviter la récurrence et les espaces inutiles
        if (clubsRepository.existsByNameIgnoreCase(clubName)) {
            throw new CustomException.AlreadyDataException("This name is unavailable");
        }

        boolean responsible = clubToUpdate.getResponsable() != null
                && Objects.equals(clubToUpdate.getResponsable().getId(), currentUser.getId()
        );

        clubToUpdate.setName(request.getClubName());
        clubToUpdate.setActive(request.isActive());

        Clubs clubUpdated = clubsRepository.save(clubToUpdate);
        return new ClubResponse(
                clubUpdated.getId(),
                clubUpdated.getName(),
                clubUpdated.getResponsable().getFirstName(),
                clubUpdated.isActive(),
                clubUpdated.getUsers().stream().anyMatch(u -> u.getId() ==  currentUser.getId()),
                responsible,
                clubUpdated.getUsers().size(),
                clubUpdated.getTournaments().size()
        );
    }

    /**
     * Allows the current user to join a club.
     *
     * @param clubId the identifier of the club to join
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no club matches the given id
     * @throws CustomException.AlreadyDataException if the current user is already a member of the club
     */
    @Transactional
    public void joinClub(int clubId) throws CustomException.AlreadyDataException {
        checkIfIdPositive(clubId);

        Users currentUser = currentUserService.getCurrentUserEntity();

        Clubs club = clubsRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("Club not found"));

        if (club.getUsers().contains(currentUser)) {
            throw new CustomException.AlreadyDataException("You are already a member of this club");
        }

        club.getUsers().add(currentUser);
        currentUser.getClubs().add(club);
    }

    /**
     * Allows the current user to leave a club.
     *
     * <p>Removes the user from all of the club's tournaments (cancelling
     * any related pending invitation) before removing them from the club
     * itself. The club's responsible manager cannot leave their own club.</p>
     *
     * @param clubId the identifier of the club to leave
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no club matches the given id, or if the current
     *         user is not a member of the club
     * @throws CustomException.InsufficientRoleException if the current user is the club's responsible manager
     */
    @Transactional
    public void leaveClub(int clubId) {

        checkIfIdPositive(clubId);

        Users currentUser = currentUserService.getCurrentUserEntity();

        Clubs club = clubsRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("Club not found"));

        if (!club.getUsers().contains(currentUser)) {
            throw new NoSuchElementException("You are not a member of this club");
        }

        if (Objects.equals(currentUser.getId(), club.getResponsable().getId())) {
            throw new CustomException.InsufficientRoleException("The club responsible cannot leave their own club");
        }


        // Retirer l'utilisateur des tournois du club
        for (Tournaments tournament : club.getTournaments()) {

            invitationRepository
                    .findByTournament_idAndUser_id(tournament.getId(), currentUser.getId())
                    .ifPresent(invitation -> invitation.setStatus(InvitationStatus.CANCELLED));

            tournament.getUsers().removeIf(user -> Objects.equals(user.getId(), currentUser.getId()));

            currentUser.getTournaments().removeIf(t -> Objects.equals(t.getId(), tournament.getId()));
        }

        // Retirer l'utilisateur du club
        club.getUsers().removeIf(user -> Objects.equals(user.getId(), currentUser.getId()));

        currentUser.getClubs().removeIf(c -> Objects.equals(c.getId(), clubId));
    }

    /**
     * Deletes an existing club.
     *
     * <p>Cascades the deletion by removing all invitations and tournaments
     * organized by the club, clearing tournament participations, and
     * removing the club from every member's club list, before deleting
     * the club itself. Only the club's responsible manager is allowed
     * to perform this action.</p>
     *
     * @param id the identifier of the club to delete
     * @throws AccessDeniedException if declared but not thrown in the current implementation
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no club matches the given id
     * @throws CustomException.InsufficientRoleException if the current user is not the club's responsible manager
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Transactional
    public void removeClub(int id) throws AccessDeniedException {

        checkIfIdPositive(id);

        UserResponse currentUser = currentUserService.getCurrentUser();

        Clubs club = clubsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Club not found"));

        if (!Objects.equals(currentUser.getId(), club.getResponsable().getId())) {
            throw new CustomException.InsufficientRoleException("You are not allowed to remove this club");
        }


        // Supprimer les invitations
        List<Tournaments> tournaments = new ArrayList<>(club.getTournaments());

        for (Tournaments tournament : tournaments){
            invitationRepository.deleteAll(invitationRepository.findAllByTournament(tournament));

            //Supprimer les participations
            for (Users user : new ArrayList<>(tournament.getUsers())){
                user.getTournaments().remove(tournament);
            }

            tournament.getUsers().clear();
        }

        // Supprimer les tournois associés
        tournamentsRepository.deleteAll(new ArrayList<>(club.getTournaments()));

        // Vider la collection côté club
        club.getTournaments().clear();

        // enlever les membres
        for (Users user : new ArrayList<>(club.getUsers())) {
            user.getClubs().removeIf(c -> Objects.equals(c.getId(), club.getId()));
        }

        club.getUsers().clear();

        clubsRepository.delete(club);
    }

    /**
     * Ensures a user with the {@code MEMBRE} role is not allowed to create a club.
     *
     * @param currentUser the user attempting to create a club
     * @throws CustomException.InsufficientRoleException if the user's role is {@code MEMBRE}
     */
    //Pour la création de club
    private void checkIfMemberNotAuthorized(Users currentUser) {
        if (currentUser.getRole().getName() == RoleType.MEMBRE) {
            throw new CustomException.InsufficientRoleException("You are not allowed to execute this action ");
        }
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