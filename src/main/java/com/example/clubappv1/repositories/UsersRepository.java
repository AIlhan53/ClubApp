package com.example.clubappv1.repositories;

import com.example.clubappv1.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data JPA repository for {@link Users} entities.
 *
 * <p>Provides lookup operations by email and by club membership, in
 * addition to the standard CRUD operations inherited from {@link JpaRepository}.</p>
 */
@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    /**
     * Retrieves a user by their exact email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching user, or empty if none is found
     */
    Optional<Users> findUserByEmail(String email);

    /**
     * Retrieves a user by their email address, ignoring case.
     *
     * @param email the email address to search for, case-insensitively
     * @return an {@link Optional} containing the matching user, or empty if none is found
     */
    Optional<Users> findByEmailIgnoreCase(String email);

    /**
     * Retrieves all users who are members of a specific club.
     *
     * @param clubId the identifier of the club
     * @return the list of users belonging to the specified club
     */
    List<Users> findAllUsersByClubs_id(int clubId);
}