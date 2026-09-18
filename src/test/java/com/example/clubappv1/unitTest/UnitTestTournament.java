package com.example.clubappv1.unitTest;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.CreateTournamentRequest;
import com.example.clubappv1.dto.TournamentResponse;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.ClubsRepository;
import com.example.clubappv1.repositories.InvitationRepository;
import com.example.clubappv1.repositories.TournamentsRepository;
import com.example.clubappv1.repositories.UsersRepository;
import com.example.clubappv1.services.CurrentUserService;
import com.example.clubappv1.services.TournamentInvitationService;
import com.example.clubappv1.services.TournamentsServices;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTestTournament {

    @Mock
    private TournamentsRepository tournamentsRepository;
    @Mock
    private ClubsRepository clubsRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private TournamentInvitationService tournamentInvitationService;

    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private TournamentsServices tournamentsServices;

    private Users existingUser;
    private Users adminUser;
    private Users responsableUser;
    private Roles memberRole;
    private Roles adminRole;
    private Roles responsableRole;

    private final Clubs club = new Clubs();

    private final Clubs club2 = new Clubs();

    private final Tournaments tournament = new Tournaments();
    private final Tournaments tournament2 = new Tournaments();

    private final TournamentInvitation invitationPending = new TournamentInvitation();

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
        existingUser.setActive(true);
        existingUser.setRole(memberRole);

        adminUser = new Users();
        adminUser.setId(10);
        adminUser.setEmail("admin@mail.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword("hashedPassword456");
        adminUser.setRole(adminRole);

        responsableUser = new Users();
        responsableUser.setId(20);
        responsableUser.setEmail("responsable@mail.com");
        responsableUser.setFirstName("responsable");
        responsableUser.setLastName("User");
        responsableUser.setPassword("hashedPassword789");
        responsableUser.setRole(responsableRole);

        club.setId(1);
        club.setName("Club Test");
        club.setResponsable(responsableUser);
        club.getTournaments().add(tournament);
        club.getTournaments().add(tournament2);

        club.getUsers().add(existingUser);
        existingUser.getClubs().add(club);

        club2.setId(2);
        club2.setName("Club Test2");
        club2.setResponsable(adminUser);

        tournament.setId(1);
        tournament.setTournamentName("Tournoi Test");
        tournament.setTournamentDescription("Description");
        tournament.setClub(club);

        tournament2.setId(2);
        tournament2.setTournamentName("Tournoi Test2");
        tournament2.setTournamentDescription("Description2");
        tournament2.setClub(club);


        tournament.getUsers().add(existingUser);
        existingUser.getTournaments().add(tournament);

        invitationPending.setId(1);
        invitationPending.setTournament(tournament);
        invitationPending.setUser(existingUser);
        invitationPending.setStatus(InvitationStatus.PENDING);

    }

    private CreateTournamentRequest buildRequest() {
        CreateTournamentRequest request = new CreateTournamentRequest();
        request.setClubId(1);
        request.setTournamentName("Nouveau Tournoi");
        request.setTournamentDescription("  Une belle description  ");
        request.setTournamentDate(tournament.getTournamentDate());
        return request;
    }
    private CreateTournamentRequest buildRequest2() {
        CreateTournamentRequest request2 = new CreateTournamentRequest();
        request2.setClubId(2);
        request2.setTournamentName("Nouveau Tournoi2");
        request2.setTournamentDescription("  Une belle description 2  ");
        request2.setTournamentDate(tournament.getTournamentDate());
        return request2;
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

    UserResponse responsableCurrentUser = createUserResponse(
            20,
            "responsable@mail.com",
            "responsableJean",
            "responsableDupont",
            RoleType.RESPONSABLE
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void getAllTournaments_shouldReturnMappedList() {
        when(currentUserService.getCurrentUser())
                .thenReturn(responsableCurrentUser);


        when(tournamentsRepository.findAll()).thenReturn(List.of(tournament));

        List<TournamentResponse> result = tournamentsServices.getAllTournaments();

        assertEquals(1, result.size());
        assertEquals(tournament.getTournamentName(), result.getFirst().getTournamentName());

        verify(tournamentsRepository).findAll();
    }

    @Test
    void getAllTournaments_shouldThrow_whenUserNotFound() {

        when(currentUserService.getCurrentUser()).thenThrow(new NoSuchElementException("Current User not found"));

        assertThrows(NoSuchElementException.class, () -> tournamentsServices.getAllTournaments());

        verify(tournamentsRepository, never()).findAll();
    }


    @Test
    void getTournamentById_shouldReturnTournament() {
        when(currentUserService.getCurrentUser())
                .thenReturn(responsableCurrentUser);

        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));

        TournamentResponse result = tournamentsServices.getTournamentById(1);

        assertNotNull(result);
        assertEquals(tournament.getTournamentName(), result.getTournamentName());
    }

    @Test
    void getTournamentById_shouldThrow_whenUserNotFound() {
        when(currentUserService.getCurrentUser()).thenThrow(new NoSuchElementException("Current User not found"));

        assertThrows(NoSuchElementException.class, () -> tournamentsServices.getTournamentById(100));
        verify(tournamentsRepository, never()).findById(anyInt());
    }

    @Test
    void getTournamentById_shouldThrow_whenTournamentNotFound() {

        when(currentUserService.getCurrentUser())
                .thenReturn(responsableCurrentUser);

        when(tournamentsRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> tournamentsServices.getTournamentById(999));
    }

    @Test
    void createTournaments_shouldCreate_whenUserIsResponsible() throws Exception {
        CreateTournamentRequest request = buildRequest();

        when(currentUserService.getCurrentUserEntity())
                .thenReturn(responsableUser);

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club));
        when(tournamentsRepository.findByTournamentNameIgnoreCase("Nouveau Tournoi"))
                .thenReturn(Optional.empty());
        when(tournamentsRepository.save(any(Tournaments.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TournamentResponse result = tournamentsServices.createTournaments(request);

        assertNotNull(result);
        assertEquals("Nouveau Tournoi", result.getTournamentName());
        assertTrue(result.isJoined());

        verify(tournamentsRepository).save(any(Tournaments.class));
    }

    @Test
    void createTournaments_shouldCreate_whenUserIsAdmin() throws Exception {
        CreateTournamentRequest request = buildRequest2();

        when(currentUserService.getCurrentUserEntity()).thenReturn(adminUser);

        when(clubsRepository.findById(2)).thenReturn(Optional.of(club2));

        when(tournamentsRepository.findByTournamentNameIgnoreCase(request.getTournamentName()))
                .thenReturn(Optional.empty());
        when(tournamentsRepository.save(any(Tournaments.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TournamentResponse result = tournamentsServices.createTournaments(request);

        assertNotNull(result);

        verify(tournamentsRepository).save(any(Tournaments.class));
    }

    @Test
    void createTournaments_shouldThrow_whenUserNotResponsibleNorAdmin() {
        Users otherUser = new Users();
        otherUser.setId(50);
        otherUser.setEmail("other@test.com");
        otherUser.setRole(memberRole);

        CreateTournamentRequest request = buildRequest();

        when(currentUserService.getCurrentUserEntity())
                .thenReturn(otherUser);

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club));

        assertThrows(CustomException.InsufficientRoleException.class, () -> tournamentsServices.createTournaments(request));

        verify(tournamentsRepository, never()).save(any());
        verify(tournamentInvitationService, never()).sendTournamentInvitations(any(), any(), any());
    }

    @Test
    void createTournaments_shouldThrow_whenTournamentNameAlreadyExists() {
        CreateTournamentRequest request = buildRequest();

        when(currentUserService.getCurrentUserEntity())
                .thenReturn(responsableUser);

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club));
        when(tournamentsRepository.findByTournamentNameIgnoreCase("Nouveau Tournoi")).thenReturn(Optional.of(tournament));

        assertThrows(CustomException.AlreadyDataException.class, () -> tournamentsServices.createTournaments(request));

        verify(tournamentsRepository, never()).save(any());
        verify(tournamentInvitationService, never()).sendTournamentInvitations(any(), any(), any());
    }

    @Test
    void createTournaments_shouldThrow_whenUserNotFound() {
        CreateTournamentRequest request = buildRequest();

        when(currentUserService.getCurrentUserEntity()).thenThrow(new NoSuchElementException("Current User not found"));

        assertThrows(NoSuchElementException.class, () -> tournamentsServices.createTournaments(request));

        verify(clubsRepository, never()).findById(anyInt());
    }

    @Test
    void createTournaments_shouldThrow_whenClubNotFound() {
        CreateTournamentRequest request = buildRequest();

        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser);

        when(clubsRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> tournamentsServices.createTournaments(request));

        verify(tournamentsRepository, never()).save(any());
    }

    @Test
    void RemoveTournament_shouldOkWhenResponsibleOfClub() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser);

        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));

        tournamentsServices.removeTournament(1);

        verify(tournamentsRepository).delete(tournament);

    }

    @Test
    void RemoveTournament_shouldTrowWhenNotResponsibleOfClub() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(adminUser);

        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> tournamentsServices.removeTournament( 1))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(tournamentsRepository, never()).delete(any());

    }

    @Test
    void acceptInvitation_shouldAccept_whenUserIsActive() throws CustomException.AlreadyDataException {

        invitationPending.setUser(existingUser);
        invitationPending.setTournament(tournament2);
        invitationPending.setStatus(InvitationStatus.PENDING);
        invitationPending.setToken("valid-token");

        // L'utilisateur doit être membre du club organisateur
        club.getUsers().add(existingUser);
        existingUser.getClubs().add(club);

        when(invitationRepository.findByToken("valid-token")).thenReturn(Optional.of(invitationPending));

        tournamentsServices.acceptInvitation(tournament2.getId(), "valid-token");

        assertEquals(InvitationStatus.ACCEPTED, invitationPending.getStatus());

        assertTrue(tournament2.getUsers().contains(existingUser));

        assertTrue(existingUser.getTournaments().contains(tournament2));
    }

    @Test
    void acceptInvitation_shouldThrow_whenUserIsDeactivated() {
        existingUser.setActive(false);

        invitationPending.setUser(existingUser);
        invitationPending.setTournament(tournament);
        invitationPending.setStatus(InvitationStatus.PENDING);
        invitationPending.setToken("valid-token");

        when(invitationRepository.findByToken("valid-token")).thenReturn(Optional.of(invitationPending));

        assertThrows(CustomException.InsufficientRoleException.class,
                () -> tournamentsServices.acceptInvitation(tournament.getId(), "valid-token")
        );

        verify(invitationRepository, never()).save(any());
    }
}
