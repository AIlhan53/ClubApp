package com.example.clubappv1.unitTest;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.RegisterRequest;
import com.example.clubappv1.dto.UpdateUserRequest;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.*;
import com.example.clubappv1.services.*;
import com.example.clubappv1.utils.JWTUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTestAuth {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private VerificationTokenService tokenService;


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

    private final RegisterRequest registerRequest = new RegisterRequest();

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


        registerRequest.setFirstName("Jean");
        registerRequest.setLastName("Dupont");
        registerRequest.setEmail("newuser@mail.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setRole(RoleType.MEMBRE);

        club1.setId(1);
        club1.setName("ClubTest");
        club1.setActive(true);
        club1.setResponsable(responsableUser1);

        club1.setUsers(new ArrayList<>());
        club1.setTournaments(new ArrayList<>());

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
    void login_shouldReturnAccessAndRefreshTokens() {

        String email = "user@mail.com";
        String password = "Password1!";

        User principal = mock(User.class);
        Authentication authentication = mock(Authentication.class);


        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(principal);

        when(jwtUtils.createToken(principal)).thenReturn("access-token");

        when(jwtUtils.createRefreshToken(principal)).thenReturn("refresh-token");

        Map<String, String> result = authServices.login(email, password);

        assertEquals("access-token", result.get("accessToken"));
        assertEquals("refresh-token", result.get("refreshToken"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtUtils).createToken(principal);
        verify(jwtUtils).createRefreshToken(principal);
    }

    @Test
    void login_shouldThrow_whenAuthenticationFails() {

        String email = "user@mail.com";
        String password = "WrongPassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authServices.login(email, password));

        verify(jwtUtils, never()).createToken(any());
        verify(jwtUtils, never()).createRefreshToken(any());
    }

    @Test
    void login_shouldAuthenticateWithProvidedCredentials() {

        String email = "user@mail.com";
        String password = "Password1!";

        Authentication authentication = mock(Authentication.class);
        User principal = mock(User.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(principal);

        when(jwtUtils.createToken(principal)).thenReturn("access");

        when(jwtUtils.createRefreshToken(principal)).thenReturn("refresh");

        authServices.login(email, password);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(
                UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(captor.capture());

        assertEquals(email, captor.getValue().getPrincipal());
        assertEquals(password, captor.getValue().getCredentials());
    }

    @Test
    void register_shouldCreateUserAndSendActivationEmail() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@mail.com");
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setPassword("Password1!");
        request.setRole(RoleType.MEMBRE);

        when(usersRepository.findByEmailIgnoreCase("newuser@mail.com")).thenReturn(Optional.empty());

        when(rolesRepository.findByName(RoleType.MEMBRE)).thenReturn(Optional.of(memberRole));

        Users savedUser = new Users();
        savedUser.setId(50);
        savedUser.setEmail("newuser@mail.com");
        savedUser.setFirstName("Jean");
        savedUser.setLastName("Dupont");
        savedUser.setRole(memberRole);
        savedUser.setActive(false);
        savedUser.setPassword("encodedPassword");

        when(passwordEncoder.encode("Password1!")).thenReturn("encodedPassword");

        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);

        VerificationToken token = new VerificationToken();
        token.setToken("verification-token");

        when(tokenService.createTokenForUser(savedUser)).thenReturn(token);

        UserResponse result = authServices.register(request);

        assertNotNull(result);
        assertEquals("newuser@mail.com", result.getEmail());
        assertEquals("Jean", result.getFirstName());

        verify(usersRepository).save(any(Users.class));
        verify(passwordEncoder).encode("Password1!");
        verify(tokenService).createTokenForUser(savedUser);

        verify(mailService).sendActivationEmail(
                eq("newuser@mail.com"),
                eq("Jean"),
                eq("verification-token")
        );
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@mail.com");
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setPassword("Password1!");
        request.setRole(RoleType.MEMBRE);

        when(usersRepository.findByEmailIgnoreCase("existing@mail.com")).thenReturn(Optional.of(existingUser));

        assertThrows(CustomException.AlreadyDataException.class, () -> authServices.register(request));

        verify(usersRepository, never()).save(any());
        verify(tokenService, never()).createTokenForUser(any());
        verify(mailService, never()).sendActivationEmail(any(), any(), any());
    }

    @Test
    void register_shouldThrow_whenRoleDoesNotExist() {


        when(usersRepository.findByEmailIgnoreCase("new@mail.com")).thenReturn(Optional.empty());

        when(rolesRepository.findByName(RoleType.MEMBRE)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authServices.register(registerRequest));

        verify(usersRepository, never()).save(any());
    }

    @Test
    void register_shouldThrow_whenRoleIsAdmin() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@mail.com");
        request.setFirstName("Admin");
        request.setLastName("User");
        request.setPassword("Password1!");
        request.setRole(RoleType.ADMIN);

        when(usersRepository.findByEmailIgnoreCase("admin@mail.com")).thenReturn(Optional.empty());

        when(rolesRepository.findByName(RoleType.ADMIN)).thenReturn(Optional.of(adminRole));

        assertThrows(CustomException.InsufficientRoleException.class, () -> authServices.register(request));

        verify(usersRepository, never()).save(any());
        verify(tokenService, never()).createTokenForUser(any());
    }

}
