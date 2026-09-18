package com.example.clubappv1.services;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.InvitationResponse;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.InvitationRepository;
import com.example.clubappv1.repositories.TournamentsRepository;
import com.example.clubappv1.repositories.UsersRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

/**
 * Service handling tournament invitation operations.
 *
 * <p>Provides operations to send invitations to club members when a
 * tournament is created, and to consult sent invitations along with
 * their status, either across all of the responsible manager's
 * tournaments or for a specific tournament.</p>
 */
@Service
public class TournamentInvitationService {

    private final UsersRepository usersRepository;
    private final TournamentsRepository tournamentsRepository;
    private final MailService mailService;
    private final InvitationRepository invitationRepository;
    private final CurrentUserService currentUserService;

    public TournamentInvitationService(UsersRepository usersRepository,
                                       InvitationRepository invitationRepository,
                                       CurrentUserService currentUserService,
                                       TournamentsRepository tournamentsRepository,
                                       MailService mailService) {
        this.usersRepository = usersRepository;
        this.invitationRepository = invitationRepository;
        this.currentUserService = currentUserService;
        this.tournamentsRepository = tournamentsRepository;
        this.mailService = mailService;
    }

    /**
     * Retrieves all tournament invitations sent for tournaments organized
     * by clubs the current user is responsible for.
     *
     * @return the list of invitations across all of the current user's tournaments
     */
    public List<InvitationResponse> getAllTournamentInvitations() {
        UserResponse responsable =
                currentUserService.getCurrentUser();

        return invitationRepository
                .findAllByTournament_Club_Responsable_id(responsable.getId())
                .stream()
                .map(invitation -> new InvitationResponse(
                        invitation.getId(),
                        invitation.getTournament().getTournamentName(),
                        invitation.getTournament().getTournamentDescription(),
                        invitation.getUser().getFirstName(),
                        invitation.getUser().getLastName(),
                        invitation.getStatus()
                ))
                .toList();
    }

    /**
     * Retrieves all invitations sent for a specific tournament.
     *
     * <p>Only the responsible manager of the organizing club (or an admin)
     * is allowed to consult this information.</p>
     *
     * @param tournamentId the identifier of the tournament
     * @return the list of invitations associated with the specified tournament
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no tournament matches the given id
     * @throws CustomException.InsufficientRoleException if the current user is not the
     *         responsible manager of the tournament's organizing club
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    public List<InvitationResponse> getInvitationsByTournamentId(int tournamentId) {

        checkIfIdPositive(tournamentId);

        UserResponse responsable = currentUserService.getCurrentUser();


        Tournaments tournament = tournamentsRepository.findById(tournamentId)
                .orElseThrow(() ->
                        new NoSuchElementException("Tournament not found"));

        if (!Objects.equals(responsable.getId(), tournament.getClub().getResponsable().getId())) {
            throw new CustomException.InsufficientRoleException("You are not allowed to send invitations for this club");
        }

        return invitationRepository
                .findAllByTournament_id(tournamentId)
                .stream()
                .map(invitation -> new InvitationResponse(
                        invitation.getId(),
                        invitation.getTournament().getTournamentName(),
                        invitation.getTournament().getTournamentDescription(),
                        invitation.getUser().getFirstName(),
                        invitation.getUser().getLastName(),
                        invitation.getStatus()
                ))
                .toList();
    }

    /**
     * Sends a tournament invitation, by email, to every member of the
     * organizing club except its responsible manager.
     *
     * <p>For each invited member, creates a pending {@link TournamentInvitation}
     * with a unique token, then sends the invitation email containing links
     * to accept or decline it.</p>
     *
     * @param tournament  the tournament members are invited to
     * @param club        the club organizing the tournament, whose members will be invited
     * @param responsible the club's responsible manager, excluded from the invitations
     * @throws CustomException.InsufficientRoleException if the current user is not the
     *         responsible manager of the given club
     */
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    public void sendTournamentInvitations(Tournaments tournament, Clubs club, Users responsible) {

        UserResponse currentUser = currentUserService.getCurrentUser();

        if (!Objects.equals(currentUser.getId(), club.getResponsable().getId())) {
            throw new CustomException.InsufficientRoleException("You are not allowed to send invitations for this club");
        }

        List<Users> members = usersRepository.findAllUsersByClubs_id(club.getId());

        for (Users member : members) {

            if (member.getId() == responsible.getId()) {
                continue;
            }

            TournamentInvitation invitation = new TournamentInvitation();

            invitation.setToken(UUID.randomUUID().toString());
            invitation.setTournament(tournament);
            invitation.setUser(member);
            invitation.setStatus(InvitationStatus.PENDING);

            invitationRepository.save(invitation);

            mailService.sendInvitationEmail(member.getEmail(), member.getFirstName(), tournament, invitation.getToken());
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