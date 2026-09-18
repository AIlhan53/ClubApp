package com.example.clubappv1.repositories;


import com.example.clubappv1.models.TournamentInvitation;
import com.example.clubappv1.models.Tournaments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TournamentInvitation} entities.
 *
 * <p>Provides lookup and deletion operations for tournament invitations by
 * token, tournament, invited user, and organizing club responsible, in
 * addition to the standard CRUD operations inherited from {@link JpaRepository}.</p>
 */
@Repository
public interface InvitationRepository extends JpaRepository<TournamentInvitation, Integer> {

    /**
     * Retrieves an invitation by its unique token.
     *
     * @param token the invitation token to search for
     * @return an {@link Optional} containing the matching invitation, or empty if none is found
     */
    Optional<TournamentInvitation> findByToken(String token);

    /**
     * Retrieves the invitation sent to a specific user for a specific tournament.
     *
     * @param tournamentId the identifier of the tournament
     * @param userId       the identifier of the invited user
     * @return an {@link Optional} containing the matching invitation, or empty if none is found
     */
    Optional<TournamentInvitation> findByTournament_idAndUser_id(int tournamentId, int userId);

    /**
     * Retrieves all invitations sent for a specific tournament.
     *
     * @param tournamentId the identifier of the tournament
     * @return the list of invitations associated with the specified tournament
     */
    List<TournamentInvitation> findAllByTournament_id(int tournamentId);

    /**
     * Retrieves all invitations sent for tournaments organized by clubs a
     * specific user is responsible for.
     *
     * @param responsableId the identifier of the club responsible
     * @return the list of invitations across all tournaments managed by the given responsible
     */
    List<TournamentInvitation> findAllByTournament_Club_Responsable_id(int responsableId);

    /**
     * Retrieves all invitations sent for a given tournament entity.
     *
     * @param tournaments the tournament whose invitations are being retrieved
     * @return the list of invitations associated with the specified tournament
     */
    List<TournamentInvitation> findAllByTournament(Tournaments tournaments);

    /**
     * Deletes all invitations sent to a specific user.
     *
     * @param userId the identifier of the invited user
     */
    void deleteAllByUser_id(int userId);
}