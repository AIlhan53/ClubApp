package com.example.clubappv1.repositories;

import com.example.clubappv1.models.Clubs;
import com.example.clubappv1.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Clubs} entities.
 *
 * <p>Provides lookup operations by name, existence checks, and queries for
 * clubs from the perspective of their responsible manager or their members,
 * in addition to the standard CRUD operations inherited from {@link JpaRepository}.</p>
 */
@Repository
public interface ClubsRepository extends JpaRepository<Clubs, Integer> {

    /**
     * Retrieves a club by its exact name.
     *
     * @param name the name of the club to search for
     * @return an {@link Optional} containing the matching club, or empty if none is found
     */
    Optional<Clubs> findClubsByName(String name);

    /**
     * Checks whether a club with the given name already exists, ignoring case.
     *
     * @param name the club name to check
     * @return {@code true} if a club with this name (case-insensitively) exists, {@code false} otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Finds all stores managed by a specific user.
     *
     * @param responsable the manager user
     * @return list of stores managed by the user
     */
    List<Clubs> findAllClubsByResponsable(Users responsable);

    /**
     * Retrieves all clubs a specific user is a member of.
     *
     * @param user the member user
     * @return the list of clubs the given user belongs to
     */
    List<Clubs> findAllClubsByUsers(Users user);
}