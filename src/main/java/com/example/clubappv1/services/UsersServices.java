package com.example.clubappv1.services;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.UpdateUserRequest;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.mapper.UserMapper;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
/**
 * Service handling user-related business logic.
 *
 * <p>Provides user administration operations (listing, activation,
 * deactivation, deletion), self/admin profile updates, and integrates
 * with Spring Security as a {@link UserDetailsService} for authentication.</p>
 */
@Service
public class UsersServices implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final ClubsRepository clubsRepository;
    private final TournamentsRepository tournamentsRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final InvitationRepository invitationRepository;

    public UsersServices(UsersRepository usersRepository,
                         RolesRepository rolesRepository,
                         ClubsRepository clubsRepository,
                         TournamentsRepository tournamentsRepository,
                         PasswordEncoder passwordEncoder,
                         CurrentUserService currentUserService,
                         InvitationRepository invitationRepository) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.clubsRepository = clubsRepository;
        this.tournamentsRepository = tournamentsRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.invitationRepository = invitationRepository;
    }

    /**
     * Retrieves all registered users.
     *
     * <p>Restricted to administrators.</p>
     *
     * @return the list of all users
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        currentUserService.getCurrentUser();

        return usersRepository.findAll().stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.isActive(),
                        u.getRole().getName().name()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single user by their identifier.
     *
     * <p>Accessible by the user themselves or by an administrator.</p>
     *
     * @param userId the identifier of the user to retrieve
     * @return the requested user
     * @throws IllegalArgumentException if the given id is not positive
     * @throws CustomException.InsufficientRoleException if the current user is neither the
     *         target user nor an admin
     * @throws NoSuchElementException if no user matches the given id
     */
    public UserResponse findUserById(int userId) {
        checkIfIdPositive(userId);

        UserResponse currentUser = currentUserService.getCurrentUser();

        checkCanExecuteAction(currentUser, userId);

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return UserMapper.toResponse(user);
    }

    /**
     * Retrieves a user entity by their email address.
     *
     * @param email the email address of the user to retrieve
     * @return the matching user entity
     * @throws UsernameNotFoundException if no user matches the given email
     */
    public Users findUserByEmail(String email) {
        return usersRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Loads a user by their username (email) for Spring Security authentication.
     *
     * @param username the email address used as the authentication username
     * @return the Spring Security {@link UserDetails} built from the matching user
     * @throws UsernameNotFoundException if no user matches the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().getName().name())
                .build();
    }

    /**
     * Updates an existing user's profile.
     *
     * <p>Accessible by the user themselves or by an administrator. First
     * name, last name, and email can be updated by either. The password
     * can only be changed by the user themselves. The role can only be
     * changed by an admin acting on another user, and promoting to
     * {@code ADMIN} requires the current user to already be an admin.
     * A user with the {@code ADMIN} role cannot be updated by anyone.</p>
     *
     * @param requestUser the updated user details
     * @param userId      the identifier of the user to update
     * @return the updated user
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no user matches the given id
     * @throws CustomException.InsufficientRoleException if the current user is not allowed
     *         to perform the requested update
     * @throws CustomException.AlreadyDataException if the new email is already used by another user
     */
    public UserResponse updateUsers(UpdateUserRequest requestUser, int userId) throws CustomException.AlreadyDataException {

        checkIfIdPositive(userId);

        UserResponse currentUser = currentUserService.getCurrentUser();

        Users userToUpdate = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        checkCanExecuteAction(currentUser, userId);

        boolean isSelf = Objects.equals(currentUser.getId(), userId);

        boolean isAdmin = RoleType.ADMIN.name().equals(currentUser.getRoleName());

        if (requestUser.getRole() == RoleType.ADMIN && !isAdmin) {
            throw new CustomException.InsufficientRoleException("You are not allowed to perform this action");
        }

        if (userToUpdate.getRole().getName() == RoleType.ADMIN) {
            throw new CustomException.InsufficientRoleException("You are not allowed to update another Administrator");
        }

        checkUpdateFirstNameUser(requestUser, userToUpdate);
        checkUpdateLastNameUser(requestUser, userToUpdate);
        checkUpdateEmailUser(requestUser, userToUpdate);

        updateUserRole(userToUpdate, requestUser, isAdmin, isSelf);

        if (requestUser.getPassword() != null && !requestUser.getPassword().isBlank()) {

            if (!isSelf) {
                throw new CustomException.InsufficientRoleException(
                        "You are not allowed to update the password of other user");
            }

            checkCanUpdatePasswordUser(requestUser, userToUpdate);
        }

        Users saved = usersRepository.save(userToUpdate);
        return UserMapper.toResponse(saved);
    }

    /**
     * Updates a user's role when requested by an admin acting on another user.
     *
     * <p>Prevents demoting a {@code RESPONSABLE} to {@code MEMBRE} while
     * they still manage at least one club.</p>
     *
     * @param userToUpdate the user whose role may be updated
     * @param requestUser  the update request, possibly containing a new role
     * @param isAdmin      whether the current user is an admin
     * @param isSelf       whether the current user is updating their own profile
     * @throws NoSuchElementException if the requested role does not exist
     * @throws CustomException.InsufficientRoleException if the user still manages clubs
     *         and the requested change would demote them from {@code RESPONSABLE} to {@code MEMBRE}
     */
    private void updateUserRole(Users userToUpdate, UpdateUserRequest requestUser, boolean isAdmin, boolean isSelf) {
        if (!isAdmin || isSelf || requestUser.getRole() == null) {
            return;
        }

        Roles newRole = rolesRepository.findByName(requestUser.getRole())
                .orElseThrow(() -> new NoSuchElementException("Role not found"));

        RoleType currentRole = userToUpdate.getRole().getName();

        if (currentRole == RoleType.RESPONSABLE && newRole.getName() == RoleType.MEMBRE
                && !userToUpdate.getClubs().isEmpty()) {

            throw new CustomException.InsufficientRoleException("This user is responsible for one or more clubs");
        }

        userToUpdate.setRole(newRole);
    }

    /**
     * Activates a user account.
     *
     * <p>Restricted to administrators.</p>
     *
     * @param userId the identifier of the user to activate
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no user matches the given id
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void activeUsers(int userId) {

        currentUserService.getEmail();

        checkIfIdPositive(userId);

        Users targetUser = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        targetUser.setActive(true);
        usersRepository.save(targetUser);
    }

    /**
     * Deactivates a user account.
     *
     * <p>Restricted to administrators. An administrator account cannot
     * be deactivated through this operation.</p>
     *
     * @param userId the identifier of the user to deactivate
     * @throws IllegalArgumentException if the given id is not positive
     * @throws NoSuchElementException if no user matches the given id
     * @throws CustomException.InsufficientRoleException if the current user is not allowed
     *         to perform this action, or if the target user is an administrator
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateUsers(int userId) {

        UserResponse currentUser = currentUserService.getCurrentUser();

        checkIfIdPositive(userId);

        Users targetUser = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        checkCanExecuteAction(currentUser, userId);

        boolean isTargetAdmin = targetUser.getRole().getName() == RoleType.ADMIN;


        if (isTargetAdmin) {
            throw new CustomException.InsufficientRoleException("You are not allowed to perform this action on an Administrator");
        }

        targetUser.setActive(false);
        usersRepository.save(targetUser);
    }

    /**
     * Deletes a user account.
     *
     * <p>Restricted to administrators. If the target user is a club
     * responsible, all clubs they manage are cascaded and deleted first
     * (provided no admin is a member of those clubs). All of the user's
     * invitations, tournament participations, and club memberships are
     * removed before the user is finally deleted.</p>
     *
     * @param userId the identifier of the user to delete
     * @throws IllegalArgumentException if the given id is not positive
     * @throws CustomException.InsufficientRoleException if the current user is not an admin,
     *         or if an admin is a member of a club managed by the target user
     * @throws NoSuchElementException if no user matches the given id
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeUser(int userId) {

        checkIfIdPositive(userId);

        UserResponse currentUser = currentUserService.getCurrentUser();

        if (!RoleType.ADMIN.name().equals(currentUser.getRoleName())) {
            throw new CustomException.InsufficientRoleException(
                    "You are not allowed to perform this action"
            );
        }

        Users targetUser = usersRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("User to remove not found"));

        if (targetUser.getRole().getName() == RoleType.RESPONSABLE) {
            checkNoAdminMemberOfManagedClubs(targetUser);

            deleteClubsManagedBy(targetUser);
        }

        // Invitations reçues par l'utilisateur
        invitationRepository.deleteAllByUser_id(userId);

        // Participations aux tournois
        for (Tournaments tournament : new ArrayList<>(targetUser.getTournaments())) {
            tournament.getUsers().remove(targetUser);
        }

        targetUser.getTournaments().clear();

        // Clubs dont il est membre
        for (Clubs club : new ArrayList<>(targetUser.getClubs())) {

            club.getUsers().remove(targetUser);
            targetUser.getClubs().remove(club);
        }

        usersRepository.delete(targetUser);
    }

    /**
     * Deletes every club managed by the given responsible user, cascading
     * the deletion to their tournaments, invitations, and participations.
     *
     * @param responsible the club responsible whose managed clubs are being deleted
     */
    private void deleteClubsManagedBy(Users responsible) {

        List<Clubs> clubs = new ArrayList<>(responsible.getClubsResponsable());

        for (Clubs club : clubs) {

            // Tournois
            for (Tournaments tournament : new ArrayList<>(club.getTournaments())) {

                invitationRepository.deleteAll(invitationRepository.findAllByTournament(tournament));

                for (Users user : new ArrayList<>(tournament.getUsers())) {

                    user.getTournaments().remove(tournament);
                }

                tournament.getUsers().clear();
            }

            List<Tournaments> tournaments = new ArrayList<>(club.getTournaments());

            tournamentsRepository.deleteAll(tournaments);
            club.getTournaments().clear();

            // Membres
            for (Users user : new ArrayList<>(club.getUsers())) {
                user.getClubs().remove(club);
            }

            club.getUsers().clear();

            // Responsable
            responsible.getClubsResponsable().remove(club);
            club.setResponsable(null);

            // Suppression
            clubsRepository.delete(club);
        }

        clubsRepository.flush();
    }

    /**
     * Ensures none of the clubs managed by the given responsible user
     * counts an administrator among its members.
     *
     * @param responsible the club responsible being checked
     * @throws CustomException.InsufficientRoleException if an administrator is a member
     *         of at least one of the responsible's managed clubs
     */
    private void checkNoAdminMemberOfManagedClubs(Users responsible) {

        for (Clubs club : responsible.getClubsResponsable()) {

            boolean adminMember = club.getUsers().stream()
                    .anyMatch(user -> user.getRole() != null && user.getRole().getName() == RoleType.ADMIN);

            if (adminMember) {
                throw new CustomException.InsufficientRoleException(
                        "This responsible cannot be deleted because an administrator " + "is a member of one of their clubs"
                );
            }
        }
    }

    /**
     * Updates a user's first name if a new, different, non-blank value was provided.
     *
     * @param requestUser  the update request
     * @param userToUpdate the user being updated
     */
    private void checkUpdateFirstNameUser(UpdateUserRequest requestUser, Users userToUpdate) {

        // pas de changement de nom demandé
        if (requestUser.getFirstName() == null || requestUser.getFirstName().isBlank()) {
            return;
        }

        // le nom n'a pas changé, rien à faire
        if (requestUser.getFirstName().equals(userToUpdate.getFirstName())) {
            return;
        }

        userToUpdate.setFirstName(requestUser.getFirstName());
    }

    /**
     * Updates a user's last name if a new, different, non-blank value was provided.
     *
     * @param requestUser  the update request
     * @param userToUpdate the user being updated
     */
    private void checkUpdateLastNameUser(UpdateUserRequest requestUser, Users userToUpdate) {

        // pas de changement de prénom demandé
        if (requestUser.getLastName() == null || requestUser.getLastName().isBlank()) {
            return;
        }

        // le prénom n'a pas changé, rien à faire
        if (requestUser.getLastName().equals(userToUpdate.getLastName())) {
            return;
        }

        userToUpdate.setLastName(requestUser.getLastName());
    }

    /**
     * Updates a user's password if a new, different, non-blank value was provided.
     *
     * @param requestUser  the update request
     * @param userToUpdate the user being updated
     */
    private void checkCanUpdatePasswordUser(UpdateUserRequest requestUser, Users userToUpdate) {

        // pas de changement de mot de passe demandé
        if (requestUser.getPassword() == null || requestUser.getPassword().isBlank()) {
            return;
        }

        // le mot de passe n'a pas changé, rien à faire
        if (passwordEncoder.matches(requestUser.getPassword(), userToUpdate.getPassword())) {
            return;
        }

        userToUpdate.setPassword(passwordEncoder.encode(requestUser.getPassword()));
    }

    /**
     * Updates a user's email if a new, different, non-blank value was provided.
     *
     * @param requestUser  the update request
     * @param userToUpdate the user being updated
     * @throws CustomException.AlreadyDataException if the new email is already used by another user
     */
    private void checkUpdateEmailUser(UpdateUserRequest requestUser, Users userToUpdate) throws CustomException.AlreadyDataException {

        // pas de changement d'email demandé
        if (requestUser.getEmail() == null || requestUser.getEmail().isBlank()) {
            return;
        }

        // l'email n'a pas changé, rien à faire
        if (requestUser.getEmail().equals(userToUpdate.getEmail())) {
            return;
        }

        // Pour éviter la récurrence et les espaces inutiles
        if (!requestUser.getEmail().equalsIgnoreCase(userToUpdate.getEmail())
                && usersRepository.findByEmailIgnoreCase(requestUser.getEmail()).isPresent()) {
            throw new CustomException.AlreadyDataException("This email already exists");
        }

        userToUpdate.setEmail(requestUser.getEmail());
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

    /**
     * Ensures the current user is allowed to act on the target user,
     * i.e. is either the target user themselves or an admin.
     *
     * @param currentUser the user attempting the action
     * @param userId      the identifier of the target user
     * @throws CustomException.InsufficientRoleException if the current user is neither the
     *         target user nor an admin
     */
    private void checkCanExecuteAction(UserResponse currentUser, int userId) {
        boolean isSelf = Objects.equals(currentUser.getId(), userId);
        boolean isAdmin = RoleType.ADMIN.name().equals(currentUser.getRoleName());

        if (!isSelf && !isAdmin) {
            throw new CustomException.InsufficientRoleException("You are not allowed to perform this action");
        }
    }
}