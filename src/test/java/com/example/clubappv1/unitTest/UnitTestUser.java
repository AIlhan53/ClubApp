package com.example.clubappv1.unitTest;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.RegisterRequest;
import com.example.clubappv1.dto.UpdateUserRequest;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.*;
import com.example.clubappv1.services.AuthServices;
import com.example.clubappv1.services.CurrentUserService;
import com.example.clubappv1.services.MailService;
import com.example.clubappv1.services.UsersServices;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTestUser {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private TournamentsRepository tournamentsRepository;

    @Mock
    private ClubsRepository clubsRepository;

    @Mock
    private MailService mailService;


    @InjectMocks
    private UsersServices usersServices;

    @InjectMocks
    private AuthServices authServices;

    private Users existingUser;
    private Users adminUser;
    private Users adminUser2;

    private final Users responsableUser1 = new Users();

    private Roles memberRole;
    private Roles adminRole;
    private Roles responsableRole;


    private final Clubs club1 = new  Clubs();

    private final Tournaments tournament = new Tournaments();

    private final TournamentInvitation invitationPending = new TournamentInvitation();
    private final TournamentInvitation invitationAccepted = new TournamentInvitation();

    @BeforeEach
    void setUp() {
        memberRole = new Roles();
        memberRole.setId(1);
        memberRole.setName(RoleType.MEMBRE);

        adminRole = new Roles();
        adminRole.setId(2);
        adminRole.setName(RoleType.ADMIN);

        responsableRole = new Roles();
        responsableRole.setId(3);
        responsableRole.setName(RoleType.RESPONSABLE);

        existingUser = new Users();
        existingUser.setId(1);
        existingUser.setEmail("test@mail.com");
        existingUser.setFirstName("Jean");
        existingUser.setLastName("Dupont");
        existingUser.setPassword("hashedPassword123");
        existingUser.setRole(memberRole);

        adminUser = new Users();
        adminUser.setId(10);
        adminUser.setEmail("admin@mail.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword("hashedPassword456");
        adminUser.setRole(adminRole);

        adminUser2 = new Users();
        adminUser2.setId(11);
        adminUser2.setEmail("admin@mail.com");
        adminUser2.setFirstName("Admin");
        adminUser2.setLastName("User");
        adminUser2.setPassword("hashedPassword456");
        adminUser2.setRole(adminRole);

        responsableUser1.setId(20);
        responsableUser1.setEmail("responsable1@mail.com");
        responsableUser1.setFirstName("responsable1");
        responsableUser1.setLastName("User1");
        responsableUser1.setPassword("hashedPassword789");
        responsableUser1.setRole(responsableRole);


        club1.setId(1);
        club1.setName("ClubTest");
        club1.setActive(true);
        club1.setResponsable(responsableUser1);

        club1.setUsers(new ArrayList<>());
        club1.getTournaments().add(tournament);

        responsableUser1.setClubsResponsable(new ArrayList<>());
        responsableUser1.getClubsResponsable().add(club1);

        club1.getUsers().add(adminUser);
        adminUser.getClubs().add(club1);

        club1.getUsers().add(existingUser);
        existingUser.getClubs().add(club1);

        tournament.setId(1);
        tournament.setTournamentName("Tournoi Test");
        tournament.setTournamentDescription("Description");
        tournament.setClub(club1);


        tournament.getUsers().add(existingUser);
        existingUser.getTournaments().add(tournament);



        invitationPending.setId(1);
        invitationPending.setTournament(tournament);
        invitationPending.setUser(existingUser);
        invitationPending.setStatus(InvitationStatus.PENDING);

        invitationAccepted.setId(2);
        invitationAccepted.setTournament(tournament);
        invitationAccepted.setUser(existingUser);
        invitationAccepted.setStatus(InvitationStatus.ACCEPTED);
    }

    private UserResponse createUserResponse(
            int id,
            String email,
            String firstName,
            String lastName,
            RoleType role
    ) {
        return new UserResponse(
                id,
                email,
                firstName,
                lastName,
                true,
                role.name()
        );
    }

    UserResponse currentUser = createUserResponse(
            1,
            "test@mail.com",
            "jean",
            "dupont",
            RoleType.MEMBRE
    );

    UserResponse adminCurrentUser = createUserResponse(
            10,
            "admin@mail.com",
            "adminJean",
            "adminDupont",
            RoleType.ADMIN
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        when(usersRepository.findUserByEmail("test@mail.com"))
                .thenReturn(Optional.of(existingUser));

        UserDetails result = usersServices.loadUserByUsername("test@mail.com");

        assertThat(result.getUsername()).isEqualTo("test@mail.com");
        assertThat(result.getPassword()).isEqualTo("hashedPassword123");
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserDoesNotExist() {
        when(usersRepository.findUserByEmail("unknown@mail.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersServices.loadUserByUsername("unknown@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }


    @Test
    void findUserById_shouldReturnUser_whenExists() {
        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));

        UserResponse result = usersServices.findUserById(1);

        assertThat(result.getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    void findUserById_shouldThrow_whenNotFoundForAdmin() {
        when(currentUserService.getCurrentUser())
                .thenReturn(adminCurrentUser);

        when(usersRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersServices.findUserById(999))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updateUsers_shouldUpdateFields_whenSelfUpdate() throws CustomException.AlreadyDataException {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("testUpdate@mail.com");
        request.setFirstName("Jean-Updated");
        request.setLastName("Dupont-Updated");

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(usersRepository.save(any(Users.class))).thenReturn(existingUser);

        usersServices.updateUsers(request, 1);

        verify(usersRepository).save(argThat(u ->
                u.getEmail().equals("testUpdate@mail.com") &&
                        u.getFirstName().equals("Jean-Updated") &&
                            u.getLastName().equals("Dupont-Updated") &&
                                u.getRole().getName().equals(RoleType.MEMBRE)
        ));
    }

    @Test
    void updateUsers_shouldUpdateFields_whenAdminUpdate() throws CustomException.AlreadyDataException {
        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("testUpdate@mail.com");
        request.setFirstName("Jean-Updated");
        request.setLastName("Dupont-Updated");
        request.setRole(RoleType.RESPONSABLE);

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(usersRepository.save(any(Users.class))).thenReturn(existingUser);
        when(rolesRepository.findByName(RoleType.RESPONSABLE))
                .thenReturn(Optional.of(responsableRole));

        usersServices.updateUsers(request, 1);

        verify(usersRepository).save(argThat(u ->
                u.getEmail().equals("testUpdate@mail.com") &&
                        u.getFirstName().equals("Jean-Updated") &&
                        u.getLastName().equals("Dupont-Updated") &&
                        u.getRole().getName().equals(RoleType.RESPONSABLE)
        ));
    }

    @Test
    void updateUsers_shouldThrow_whenNotAdminAndChangeSelfRoleToAdmin(){
        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("testUpdate@mail.com");
        request.setFirstName("Jean-Updated");
        request.setLastName("Dupont-Updated");
        request.setRole(RoleType.ADMIN);

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> usersServices.updateUsers(request, 1))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(usersRepository, never()).save(any());
    }
    @Test
    void updateUsers_shouldThrow_whenNotSelfAndNotAdmin() {
        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("other@mail.com");
        request.setFirstName("Autre");
        request.setLastName("Personne");

        Users otherUser = new Users();
        otherUser.setId(2);
        otherUser.setEmail("other@mail.com");
        otherUser.setRole(memberRole);

        when(usersRepository.findById(2)).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> usersServices.updateUsers(request, 2))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(usersRepository, never()).save(any());
    }

    @Test
    void updateUsers_shouldThrow_whenNotSelfAndAdminForUpdatingPassword() {
        when(currentUserService.getCurrentUser())
                .thenReturn(adminCurrentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("other@mail.com");
        request.setFirstName("Autre");
        request.setLastName("Personne");
        request.setPassword("NewPassword");

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> usersServices.updateUsers(request, 1))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(usersRepository, never()).save(any());
    }

    @Test
    void updateUsers_shouldThrow_whenAdminForUpdateAnotherAdmin() {
        when(currentUserService.getCurrentUser())
                .thenReturn(adminCurrentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("other@mail.com");
        request.setFirstName("Autre");
        request.setLastName("Personne");

        when(usersRepository.findById(11)).thenReturn(Optional.of(adminUser2));

        assertThatThrownBy(() -> usersServices.updateUsers(request, 11))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(usersRepository, never()).save(any());
    }

    @Test
    void updateUsers_shouldThrow_whenEmailAlreadyUsedByAnotherUser() {
        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("taken@mail.com");
        request.setFirstName("Jean");
        request.setLastName("Dupont");

        Users anotherUser = new Users();
        anotherUser.setId(2);
        anotherUser.setEmail("taken@mail.com");

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmailIgnoreCase("taken@mail.com"))
                .thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> usersServices.updateUsers(request, 1))
                .isInstanceOf(CustomException.AlreadyDataException.class);
    }

    @Test
    void updateUsers_shouldHashNewPassword_whenPasswordProvided() throws CustomException.AlreadyDataException {
        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("test@mail.com");
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setPassword("newPassword123");

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("newPassword123", "hashedPassword123")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(existingUser);

        usersServices.updateUsers(request, 1);

        verify(passwordEncoder).encode("newPassword123");
        verify(usersRepository).save(argThat(u -> u.getPassword().equals("newHashedPassword")));
    }

    @Test
    void updateUsers_shouldNotRehash_whenPasswordUnchanged() throws CustomException.AlreadyDataException {
        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("test@mail.com");
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setPassword("samePassword");

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("samePassword", "hashedPassword123")).thenReturn(true);
        when(usersRepository.save(any(Users.class))).thenReturn(existingUser);

        usersServices.updateUsers(request, 1);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void removeUsers_shouldOkWhenAdmin() {
        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        when(usersRepository.findById(1)).thenReturn(Optional.of(existingUser));

        usersServices.removeUser(1);

        verify(usersRepository).delete(existingUser);
    }

    @Test
    void removeUser_shouldRemoveUserFromClubsAndTournamentsAndInvitations() {

        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        when(usersRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));

        assertEquals(club1, tournament.getClub());
        assertTrue(club1.getUsers().contains(existingUser));
        assertTrue(tournament.getUsers().contains(existingUser));

        // Vérifie que l'utilisateur possède bien des invitations
        assertEquals(existingUser, invitationPending.getUser());
        assertEquals(existingUser, invitationAccepted.getUser());

        usersServices.removeUser(existingUser.getId());

        // L'utilisateur est retiré de ses relations
        assertFalse(club1.getUsers().contains(existingUser), "User should be removed from the club");

        assertFalse(existingUser.getClubs().contains(club1), "Club should be removed from user's clubs");

        assertFalse(tournament.getUsers().contains(existingUser), "User should be removed from tournament participants");

        assertTrue(existingUser.getTournaments().isEmpty(), "User should no longer have tournament participations");

        // Les invitations de l'utilisateur sont supprimées
        verify(invitationRepository).deleteAllByUser_id(existingUser.getId());

        // L'utilisateur est supprimé
        verify(usersRepository).delete(existingUser);
    }

    @Test
    void removeUsers_shouldThrowWhenNotAdmin() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> usersServices.removeUser(1))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(usersRepository, never()).delete(any());
    }

    @Test
    void removeUser_shouldThrow_whenResponsibleHasAdminMemberInClub() {

        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        when(usersRepository.findById(responsableUser1.getId())).thenReturn(Optional.of(responsableUser1));

        assertThrows(CustomException.InsufficientRoleException.class, ()
                -> usersServices.removeUser(responsableUser1.getId())
        );

        verify(clubsRepository, never()).delete(any());
        verify(usersRepository, never()).delete(any());
    }
}
