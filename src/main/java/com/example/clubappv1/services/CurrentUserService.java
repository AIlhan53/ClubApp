package com.example.clubappv1.services;

import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.mapper.UserMapper;
import com.example.clubappv1.models.Users;
import com.example.clubappv1.repositories.UsersRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
/**
 * Utility component providing access to the currently authenticated user.
 *
 * <p>Reads the authentication information from the Spring Security context
 * and resolves it to the corresponding {@link Users} entity or DTO.</p>
 */
@Component
public class CurrentUserService {

    private final UsersRepository usersRepository;

    public CurrentUserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    /**
     * Retrieves the email of the currently authenticated user.
     *
     * @return the email (username) of the authenticated user
     * @throws AccessDeniedException if there is no authenticated user in the security context
     */
    public String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        return auth.getName();
    }

    /**
     * Retrieves the currently authenticated user as a response DTO.
     *
     * @return the current user, mapped to a response DTO
     * @throws AccessDeniedException if there is no authenticated user in the security context
     * @throws NoSuchElementException if no user matches the authenticated email
     */
    public UserResponse getCurrentUser() {
        Users user = usersRepository.findUserByEmail(getEmail())
                .orElseThrow(() -> new NoSuchElementException("Current User not found"));

        return UserMapper.toResponse(user);
    }

    /**
     * Retrieves the currently authenticated user as a managed entity.
     *
     * @return the current user entity
     * @throws AccessDeniedException if there is no authenticated user in the security context
     * @throws NoSuchElementException if no user matches the authenticated email
     */
    public Users getCurrentUserEntity() {
        return usersRepository.findUserByEmail(getEmail())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
