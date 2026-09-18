package com.example.clubappv1.repositories;


import com.example.clubappv1.models.Tournaments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Tournaments} entities.
 *
 * <p>Provides lookup operations by tournament name and by organizing club,
 * in addition to the standard CRUD operations inherited from {@link JpaRepository}.</p>
 */
@Repository
public interface TournamentsRepository extends JpaRepository<Tournaments, Integer> {

    /**
     * Retrieves a tournament by its name, ignoring case.
     *
     * @param name the tournament name to search for, case-insensitively
     * @return an {@link Optional} containing the matching tournament, or empty if none is found
     */
    Optional<Tournaments> findByTournamentNameIgnoreCase(String name);

    /**
     * Retrieves all tournaments organized by a specific club.
     *
     * @param clubId the identifier of the club
     * @return the list of tournaments organized by the specified club
     */
    List<Tournaments> findAllTournamentsByClub_id(Integer clubId);
}