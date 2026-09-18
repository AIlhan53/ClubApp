package com.example.clubappv1.unitTest;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.*;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.*;
import com.example.clubappv1.services.ClubServices;
import com.example.clubappv1.services.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTestClub {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private ClubsRepository clubsRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private TournamentsRepository tournamentsRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClubServices  clubServices;

    private Users existingUser;
    private Users adminUser;
    private Users responsableUser1;
    private Users responsableUser2;

    private Roles memberRole;
    private Roles adminRole;
    private Roles responsableRole;


    private Clubs club1;
    private Clubs club2;

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

        club1 = new Clubs();
        club1.setId(1);
        club1.setName("ClubTest");
        club1.setActive(true);
        club1.setResponsable(responsableUser1);
        club1.setUsers(new ArrayList<>());
        club1.getTournaments().add(tournament);

        club2 = new Clubs();
        club2.setId(2);
        club2.setName("ClubTest2");
        club2.setResponsable(responsableUser2);
        club2.setActive(true);
        club2.setUsers(new ArrayList<>());
        club2.setTournaments(new ArrayList<>());

        club1.getUsers().add(responsableUser1);
        responsableUser1.setClubsResponsable(new ArrayList<>());
        responsableUser1.getClubsResponsable().add(club1);

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

    UserResponse responsableCurrentUser1 = createUserResponse(
            20,
            "responsable1@mail.com",
            "responsable1",
            "User1",
            RoleType.RESPONSABLE
    );

    UserResponse responsableCurrentUser2 = createUserResponse(
            21,
            "responsable2@mail.com",
            "responsable2",
            "User2",
            RoleType.RESPONSABLE
    );


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllClub_shouldReturnMappedList() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of(club1));

        List<ClubResponse> result = clubServices.getAllClub();

        assertEquals(1, result.size());
        assertEquals(club1.getName(), result.getFirst().getClubName());

        verify(clubsRepository).findAll();
    }

    @Test
    void getAllClub_shouldReturnEmptyList_whenNoClub() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of());

        List<ClubResponse> result = clubServices.getAllClub();

        assertTrue(result.isEmpty());

        verify(clubsRepository).findAll();
    }

    @Test
    void getAllClub_shouldSetJoinedTrue_whenUserIsMember() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of(club1));

        List<ClubResponse> result = clubServices.getAllClub();

        assertTrue(result.getFirst().isJoined());
    }

    @Test
    void getAllClub_shouldSetJoinedFalse_whenUserIsNotMember() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser2);;

        when(clubsRepository.findAll()).thenReturn(List.of(club1));

        List<ClubResponse> result = clubServices.getAllClub();

        assertFalse(result.getFirst().isJoined());
    }

    @Test
    void getAllClub_shouldSetResponsibleTrue_whenUserIsResponsible() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of(club1));

        List<ClubResponse> result = clubServices.getAllClub();

        assertTrue(result.getFirst().isResponsible());
    }

    @Test
    void getAllClub_shouldSetResponsibleFalse_forAnotherResponsibleClub() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of(club2));

        List<ClubResponse> result = clubServices.getAllClub();

        assertFalse(result.getFirst().isResponsible());
    }

    @Test
    void getAllClub_shouldReturnAllClubs() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of(club1, club2));

        List<ClubResponse> result = clubServices.getAllClub();

        assertEquals(2, result.size());

        assertEquals("ClubTest", result.get(0).getClubName());
        assertEquals("ClubTest2", result.get(1).getClubName());
    }
    @Test
    void getAllClub_shouldMarkClubAsResponsible_whenCurrentUserIsResponsible() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        club1.setResponsable(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of(club1));

        List<ClubResponse> result = clubServices.getAllClub();

        assertEquals(1, result.size());
        assertTrue(result.getFirst().isResponsible());
    }

    @Test
    void getAllClub_shouldNotMarkClubAsResponsible_whenCurrentUserIsNotResponsible() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        club1.setResponsable(responsableUser2);

        when(clubsRepository.findAll()).thenReturn(List.of(club1));

        List<ClubResponse> result = clubServices.getAllClub();

        assertEquals(1, result.size());
        assertFalse(result.getFirst().isResponsible());
    }

    @Test
    void getAllClub_shouldReturnCorrectMemberCount() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAll()).thenReturn(List.of(club1));

        List<ClubResponse> result = clubServices.getAllClub();

        assertEquals(2, result.getFirst().getMemberCount());
    }

    @Test
    void getAllTournaments_shouldThrow_whenUserNotFound() {

        when(currentUserService.getCurrentUserEntity()).thenThrow(new NoSuchElementException("Current User not found"));

        assertThrows(NoSuchElementException.class, () ->  clubServices.getAllClub());

        verify(clubsRepository, never()).findAll();
    }

    @Test
    void createClub_shouldAllowResponsible() throws CustomException.AlreadyDataException {
        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.save(any(Clubs.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateClubRequest request = new CreateClubRequest();
        request.setClubName("Nouveau Club");

        ClubResponse result = clubServices.createClub(request);

        assertNotNull(result);
        assertEquals("Nouveau Club", result.getClubName());
        assertEquals(responsableUser1.getFirstName(), result.getResponsableName());

        verify(clubsRepository).save(any(Clubs.class));
    }

    @Test
    void createClub_shouldThrow_whenUserIsMember() {
        when(currentUserService.getCurrentUserEntity()).thenReturn(existingUser);

        CreateClubRequest request = new CreateClubRequest();
        request.setClubName("Club interdit");

        assertThrows(CustomException.InsufficientRoleException.class, () -> clubServices.createClub(request));

        verify(clubsRepository, never()).save(any(Clubs.class));
    }

    @Test
    void createClub_shouldAllowAdmin() throws CustomException.AlreadyDataException {
        when(currentUserService.getCurrentUserEntity()).thenReturn(adminUser);

        when(clubsRepository.save(any(Clubs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateClubRequest request = new CreateClubRequest();
        request.setClubName("Club Admin");

        ClubResponse result = clubServices.createClub(request);

        assertNotNull(result);
        assertEquals("Club Admin", result.getClubName());
        assertEquals(adminUser.getFirstName(), result.getResponsableName());

        verify(clubsRepository).save(any(Clubs.class));
    }

    @Test
    void UpdateName_shouldReturnOkWhenResponsible() throws CustomException.AlreadyDataException {
        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser1);

        UpdateClubRequest clubRequest = new UpdateClubRequest();
        clubRequest.setClubName("updatedClub");

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club1));
        when(clubsRepository.save(any(Clubs.class))).thenReturn(club1);

        clubServices.updateClub(1, clubRequest);

        verify(clubsRepository).save(argThat(c ->
                c.getName().equals("updatedClub")
        ));
    }

    @Test
    void UpdateName_shouldReturnThrowExceptionWhenClubNameIsUnavailable() {
        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser1);

        UpdateClubRequest clubRequest = new UpdateClubRequest();
        clubRequest.setClubName("ClubTest2");

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club1));
        when(clubsRepository.existsByNameIgnoreCase(clubRequest.getClubName()))
                .thenReturn(club2.getName().equals(clubRequest.getClubName()));

        assertThatThrownBy(() -> clubServices.updateClub( 1,clubRequest))
                .isInstanceOf(CustomException.AlreadyDataException.class);

        verify(clubsRepository, never()).save(any());
    }

    @Test
    void UpdateName_shouldReturnThrowExceptionWhenNoResponsibleAndAdmin() {
        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        UpdateClubRequest clubRequest = new UpdateClubRequest();
        clubRequest.setClubName("updatedClub");

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club1));

        assertThatThrownBy(() -> clubServices.updateClub( 1,clubRequest))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(clubsRepository, never()).save(any());
    }

    @Test
    void UpdateName_shouldReturnThrowExceptionWhenNoResponsible() {
        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser2);

        UpdateClubRequest clubRequest = new UpdateClubRequest();
        clubRequest.setClubName("updatedClub");

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club1));

        assertThatThrownBy(() -> clubServices.updateClub( 1,clubRequest))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(clubsRepository, never()).save(any());
    }

    @Test
    void RemoveClub_shouldReturnOkWhenResponsible() {
        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser1);

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club1));

        clubServices.removeClub(1);

        verify(clubsRepository).delete(club1);
    }

    @Test
    void RemoveClub_shouldReturnThrowWhenNotResponsible() {
        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser2);

        when(clubsRepository.findById(1)).thenReturn(Optional.of(club1));

        assertThatThrownBy(() -> clubServices.removeClub( 1))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(clubsRepository, never()).delete(any());
    }

    @Test
    void removeClub_shouldRemoveClubAndTournamentsAndInvitations() {

        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser1);
        when(clubsRepository.findById(1)).thenReturn(Optional.of(club1));

        assertEquals(club1, tournament.getClub());
        assertTrue(club1.getUsers().contains(existingUser));
        assertTrue(tournament.getUsers().contains(existingUser));

        assertEquals(existingUser, invitationPending.getUser());
        assertEquals(existingUser, invitationAccepted.getUser());

        clubServices.removeClub(club1.getId());

        // L'utilisateur est retiré de ses relations

        assertTrue(tournament.getUsers().isEmpty(), "Tournament should no longer have participants");

        assertTrue(existingUser.getTournaments().isEmpty(), "User should no longer participate in tournaments");

        assertTrue(club1.getUsers().isEmpty(), "Club should no longer have members");

        assertTrue(existingUser.getClubs().isEmpty(), "User should no longer belong to the club");

        // Le club est supprimé
        verify(clubsRepository).delete(club1);
    }
}
