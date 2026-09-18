package com.example.clubappv1.controllers;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.UpdateUserRequest;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.services.CurrentUserService;
import com.example.clubappv1.services.UsersServices;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller responsible for user management.
 *
 * This controller provides endpoints for:
 * <ul>
 *     <li>Retrieving users (admin and self-access)</li>
 *     <li>Updating user information with role-based restrictions</li>
 *     <li>Activating, deactivating and deleting user accounts</li>
 * </ul>
 *
 * Security rules:
 * <ul>
 *     <li>Only administrators can manage other users</li>
 *     <li>Administrators cannot modify or delete other administrators</li>
 *     <li>Non-admin users can only access or modify their own account</li>
 * </ul>
 */

@RestController
@RequestMapping(path = "/user")
public class UserController {

    private final UsersServices userServices;
    private final CurrentUserService currentUserService;

    public UserController(UsersServices userServices,
                          CurrentUserService currentUserService) {
        this.userServices = userServices;
        this.currentUserService = currentUserService;
    }

    /**
     * Retrieves all users.
     * Access restricted to administrators only.
     * @return list of all users
     * @throws RuntimeException if the authenticated user cannot be found
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userServices.getAllUsers());
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return authenticated user entity
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves a user by ID.
     * <p>
     * A user can access their own account.
     * Administrators can access any user account.
     *
     * @param id user ID
     * @return user entity if authorized
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userServices.findUserById(id));
    }

    /**
     * Request that update a user.
     * only administrator can modify role
     * @param id id of the updated user
     * @param request type UpdateUserDTO
     * @return ResponseEntity with the user Response
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable int id, @Valid @RequestBody UpdateUserRequest request) throws CustomException.AlreadyDataException {
        return ResponseEntity.ok(userServices.updateUsers(request, id));
    }

    /**
     * Deactivates a user account.
     * A user may deactivate their own account.
     * Administrators may deactivate non-admin users.
     *
     * @param id user ID
     * @return deactivation confirmation message
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable int id) {
        userServices.deactivateUsers(id);
        return ResponseEntity.ok(Map.of("message", "Account deactivated"));
    }

    /**
     * Activates a user account.
     * Access restricted to administrators.
     *
     * @param id user ID
     * @return activation confirmation message
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable int id) {
        userServices.activeUsers(id);
        return ResponseEntity.ok(Map.of("message", "Account activated"));

    }

    /**
     * Deletes a user account.
     * <p>
     * Only administrators are allowed to delete users.
     * Administrators cannot delete other administrators.
     * Delete all about this user.
     *
     * @param id user ID
     * @return deletion confirmation message
     */
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeUser(@PathVariable int id) {
        userServices.removeUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
