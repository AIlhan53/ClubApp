package com.example.clubappv1.repositories;

import com.example.clubappv1.models.RoleType;
import com.example.clubappv1.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Roles} entities.
 *
 * <p>Provides lookup of a role by its type, in addition to the standard
 * CRUD operations inherited from {@link JpaRepository}.</p>
 */
@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer> {

    /**
     * Retrieves a role by its type.
     *
     * @param name the role type to search for
     * @return an {@link Optional} containing the matching role, or empty if none is found
     */
    Optional<Roles> findByName(RoleType name);
}