package com.example.clubappv1.unitTest;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.InvitationResponse;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.InvitationRepository;
import com.example.clubappv1.repositories.RolesRepository;
import com.example.clubappv1.repositories.TournamentsRepository;
import com.example.clubappv1.repositories.UsersRepository;
import com.example.clubappv1.services.CurrentUserService;
import com.example.clubappv1.services.MailService;
import com.example.clubappv1.services.TournamentInvitationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTestInvitationTournament {

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
    private MailService mailService;


    @InjectMocks
    private TournamentInvitationService tournamentInvitationService;

    private Users existingUser;
    private Users adminUser;
    private Users adminUser2;
    private Users responsableUser1;
    private Users responsableUser2;


    private Roles memberRole;
    private Roles adminRole;
    private Roles responsableRole;

    private final Clubs club1 = new Clubs();
    private final Clubs club2 = new Clubs();

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

        responsableUser1 = new Users();
        responsableUser1.setId(20);
        responsableUser1.setEmail("responsable1@mail.com");
        responsableUser1.setFirstName("responsable1");
        responsableUser1.setLastName("User1");
        responsableUser1.setPassword("hashedPassword789");
        responsableUser1.setRole(responsableRole);

        responsableUser2 = new Users();
        responsableUser2.setId(21);
        responsableUser2.setEmail("responsable2@mail.com");
        responsableUser2.setFirstName("responsable2");
        responsableUser2.setLastName("User2");
        responsableUser2.setPassword("hashedPassword2");
        responsableUser2.setRole(responsableRole);


        club1.setId(1);
        club1.setName("ClubTest");
        club1.setActive(true);
        club1.setResponsable(responsableUser1);
        club1.getUsers().add(existingUser);

        club1.getTournaments().add(tournament);
        existingUser.getClubs().add(club1);

        club2.setId(2);
        club2.setName("ClubTest2");
        club2.setResponsable(responsableUser2);
        club2.setActive(true);
        club2.setUsers(new ArrayList<>());
        club2.setTournaments(new ArrayList<>());

        tournament.setId(1);
        tournament.setTournamentName("TournoiTest");
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

    UserResponse currentresponsible1 = createUserResponse(
            20,
            "responsable1@mail.com",
            "responsable1",
            "User1",
            RoleType.RESPONSABLE
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllTournamentInvitations_shouldReturnInvitationsForResponsible() {

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(invitationRepository.findAllByTournament_Club_Responsable_id(currentresponsible1.getId()))
                .thenReturn(List.of(invitationPending));

        List<InvitationResponse> result = tournamentInvitationService.getAllTournamentInvitations();

        assertEquals(1, result.size());

        InvitationResponse response = result.getFirst();

        assertEquals(1, response.getId());
        assertEquals("TournoiTest", response.getTournamentName());
        assertEquals("Description", response.getTournamentDescription());
        assertEquals("Jean", response.getFirstNameMember());
        assertEquals("Dupont", response.getLastNameMember());
        assertEquals(InvitationStatus.PENDING, response.getInvitationStatus());

        verify(invitationRepository).findAllByTournament_Club_Responsable_id(20);
    }

    @Test
    void getAllTournamentInvitations_shouldReturnEmptyList_whenNoInvitation() {

        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        when(invitationRepository.findAllByTournament_Club_Responsable_id(10)).thenReturn(List.of());

        List<InvitationResponse> result = tournamentInvitationService.getAllTournamentInvitations();

        assertTrue(result.isEmpty());

        verify(invitationRepository).findAllByTournament_Club_Responsable_id(10);
    }

    @Test
    void getAllTournamentInvitations_shouldThrow_whenCurrentUserNotFound() {

        when(currentUserService.getCurrentUser()).thenThrow(new NoSuchElementException("Current User not found"));

        assertThrows(NoSuchElementException.class, () -> tournamentInvitationService.getAllTournamentInvitations());

        verify(invitationRepository, never()).findAllByTournament_Club_Responsable_id(anyInt());
    }

    @Test
    void getInvitationsByTournamentId_shouldReturnInvitations() {

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));

        when(invitationRepository.findAllByTournament_id(1)).thenReturn(List.of(invitationAccepted));

        List<InvitationResponse> result = tournamentInvitationService.getInvitationsByTournamentId(1);

        assertEquals(1, result.size());
        assertEquals(InvitationStatus.ACCEPTED, result.getFirst().getInvitationStatus());

        verify(invitationRepository).findAllByTournament_id(1);
    }

    @Test
    void getInvitationsByTournamentId_shouldReturnEmptyList_whenNoInvitation() {

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));

        when(invitationRepository.findAllByTournament_id(1)).thenReturn(List.of());

        List<InvitationResponse> result = tournamentInvitationService.getInvitationsByTournamentId(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getInvitationsByTournamentId_shouldThrow_whenTournamentNotFound() {

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(tournamentsRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> tournamentInvitationService.getInvitationsByTournamentId(999));

        verify(invitationRepository, never()).findAllByTournament_id(anyInt());
    }

    @Test
    void getInvitationsByTournamentId_shouldThrow_whenTournamentIdIsInvalid() {

        assertThrows(IllegalArgumentException.class, () -> tournamentInvitationService.getInvitationsByTournamentId(0));

        verify(tournamentsRepository, never()).findById(anyInt());
    }

    @Test
    void getInvitationsByTournamentId_shouldThrow_whenUserIsNotTournamentResponsible() {

        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));

        assertThrows(CustomException.InsufficientRoleException.class,
                () -> tournamentInvitationService.getInvitationsByTournamentId(1));

        verify(invitationRepository, never()).findAllByTournament_id(anyInt());
    }

    @Test
    void sendTournamentInvitations_shouldSendInvitationToMembersExceptResponsible() {

        List<Users> members = List.of(existingUser, responsableUser1);

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(usersRepository.findAllUsersByClubs_id(club1.getId())).thenReturn(members);

        tournamentInvitationService.sendTournamentInvitations(tournament, club1, responsableUser1);

        verify(invitationRepository, times(1))
                .save(any(TournamentInvitation.class));

        verify(mailService, times(1))
                .sendInvitationEmail(
                        eq(existingUser.getEmail()),
                        eq(existingUser.getFirstName()),
                        eq(tournament),
                        anyString());
    }

    @Test
    void sendTournamentInvitations_shouldNotInviteResponsible(){

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(usersRepository.findAllUsersByClubs_id(club1.getId())).thenReturn(List.of(responsableUser1));

        tournamentInvitationService.sendTournamentInvitations(tournament, club1, responsableUser1);

        verify(invitationRepository, never())
                .save(any(TournamentInvitation.class));

        verify(mailService, never())
                .sendInvitationEmail(
                        anyString(),
                        anyString(),
                        any(Tournaments.class),
                        anyString()
                );
    }

    @Test
    void sendTournamentInvitations_shouldDoNothing_whenNoMembers() {

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(usersRepository.findAllUsersByClubs_id(club1.getId())).thenReturn(List.of());

        tournamentInvitationService.sendTournamentInvitations(tournament, club1, responsableUser1);

        verify(invitationRepository, never()).save(any());

        verify(mailService, never())
                .sendInvitationEmail(
                        anyString(),
                        anyString(),
                        any(),
                        anyString()
                );
    }

    @Test
    void sendTournamentInvitations_shouldCreatePendingInvitation() {

        when(currentUserService.getCurrentUser()).thenReturn(currentresponsible1);

        when(usersRepository.findAllUsersByClubs_id(club1.getId())).thenReturn(List.of(existingUser));

        tournamentInvitationService.sendTournamentInvitations(tournament, club1, responsableUser1);

        ArgumentCaptor<TournamentInvitation> captor = ArgumentCaptor.forClass(TournamentInvitation.class);

        verify(invitationRepository).save(captor.capture());

        TournamentInvitation invitation = captor.getValue();

        assertNotNull(invitation.getToken());
        assertFalse(invitation.getToken().isBlank());

        assertEquals(tournament, invitation.getTournament());
        assertEquals(existingUser, invitation.getUser());
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
    }

    @Test
    void sendTournamentInvitations_shouldThrow_whenCurrentUserIsNotClubResponsible() {

        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        assertThrows(CustomException.InsufficientRoleException.class, () -> tournamentInvitationService.sendTournamentInvitations(tournament, club1, responsableUser1));

        verify(invitationRepository, never()).save(any());
        verify(mailService, never()).sendInvitationEmail(anyString(), anyString(),
                any(Tournaments.class),
                anyString()
        );
    }
}
