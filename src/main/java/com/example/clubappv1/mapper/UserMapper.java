package com.example.clubappv1.mapper;

import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.Users;

/**
 * Utility mapper converting {@link Users} entities into {@link UserResponse} DTOs.
 */
public class UserMapper {

    /**
     * Maps a {@link Users} entity to its response DTO representation.
     *
     * @param user the user entity to map
     * @return the corresponding {@link UserResponse}
     */
    public static UserResponse toResponse(Users user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                user.getRole().getName().name()
        );
    }
}