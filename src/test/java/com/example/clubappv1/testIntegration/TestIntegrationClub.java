package com.example.clubappv1.testIntegration;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.ClubMemberResponse;
import com.example.clubappv1.dto.ClubResponse;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.Clubs;
import com.example.clubappv1.models.RoleType;
import com.example.clubappv1.models.Roles;
import com.example.clubappv1.models.Users;
import com.example.clubappv1.repositories.ClubsRepository;
import com.example.clubappv1.repositories.UsersRepository;
import com.example.clubappv1.services.ClubServices;
import com.example.clubappv1.services.CurrentUserService;
import com.example.clubappv1.services.UsersServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TestIntegrationClub {

    @MockitoBean
    private ClubsRepository clubsRepository;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private CurrentUserService currentUserService;

    @Autowired
    private ClubServices clubsServices;

    private Users existingUser;
    private Users adminUser;

    private Users responsableUser1;
    private Users responsableUser2;


    private Roles memberRole;
    private Roles adminRole;
    private Roles responsableRole;

    private Clubs club1;

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
        club1.setTournaments(new ArrayList<>());


        club1.getUsers().add(responsableUser1);
        responsableUser1.setClubsResponsable(new ArrayList<>());
        responsableUser1.getClubsResponsable().add(club1);
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


    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void getClubsByResponsableWhenResponsible_shouldReturnOwnClubs() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findAllClubsByResponsable(responsableUser1)).thenReturn(List.of(club1));

        List<ClubResponse> result = clubsServices.getClubsByResponsable();

        assertEquals(1, result.size());
        assertEquals(club1.getName(), result.getFirst().getClubName());
        assertTrue(result.getFirst().isResponsible());

        verify(clubsRepository).findAllClubsByResponsable(responsableUser1);
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void getClubsByResponsableWhenMember_shouldThrow() {

        assertThatThrownBy(() -> clubsServices.getClubsByResponsable()).isInstanceOf(AccessDeniedException.class);

        verify(clubsRepository, never()).findAllClubsByResponsable(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClubsByResponsableWhenAdmin_shouldBeAllowed() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(adminUser);

        when(clubsRepository.findAllClubsByResponsable(adminUser)).thenReturn(List.of());

        List<ClubResponse> result = clubsServices.getClubsByResponsable();

        assertTrue(result.isEmpty());

        verify(clubsRepository).findAllClubsByResponsable(adminUser);
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void getAllUserByClubIdWhenResponsible_shouldReturnMembers() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findById(club1.getId())).thenReturn(Optional.of(club1));

        when(usersRepository.findAllUsersByClubs_id(club1.getId())).thenReturn(List.of(existingUser));

        List<ClubMemberResponse> result = clubsServices.getAllUserByClubId(club1.getId());

        assertEquals(1, result.size());
        assertEquals(existingUser.getFirstName(), result.getFirst().getFirstName());
        assertEquals(existingUser.getLastName(), result.getFirst().getLastName());

        verify(usersRepository).findAllUsersByClubs_id(club1.getId());
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void getAllUserByClubIdWhenNotResponsible_shouldThrow() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser2);

        when(clubsRepository.findById(club1.getId())).thenReturn(Optional.of(club1));

        assertThatThrownBy(() -> clubsServices.getAllUserByClubId(club1.getId()))
                .isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(usersRepository, never()).findAllUsersByClubs_id(anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUserByClubIdWhenAdmin_shouldBeAllowed() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(adminUser);

        when(clubsRepository.findById(club1.getId())).thenReturn(Optional.of(club1));

        when(usersRepository.findAllUsersByClubs_id(club1.getId())).thenReturn(List.of(existingUser));

        List<ClubMemberResponse> result = clubsServices.getAllUserByClubId(club1.getId());

        assertEquals(1, result.size());

        verify(usersRepository).findAllUsersByClubs_id(club1.getId());
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void getAllUserByClubIdWhenClubNotFound_shouldThrow() {

        when(currentUserService.getCurrentUserEntity()).thenReturn(responsableUser1);

        when(clubsRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubsServices.getAllUserByClubId(999)).isInstanceOf(NoSuchElementException.class);

        verify(usersRepository, never()).findAllUsersByClubs_id(anyInt());
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void removeClubWhenResponsible_shouldBeAllowed() {

        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser1);

        when(clubsRepository.findById(club1.getId())).thenReturn(Optional.of(club1));

        clubsServices.removeClub(club1.getId());

        verify(clubsRepository).delete(club1);
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void removeClubWhenNotResponsible_shouldThrow() {

        when(currentUserService.getCurrentUser()).thenReturn(responsableCurrentUser2);

        when(clubsRepository.findById(club1.getId())).thenReturn(Optional.of(club1));

        assertThatThrownBy(() -> clubsServices.removeClub(club1.getId())).isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(clubsRepository, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void removeClubWhenMember_shouldBeDenied() {

        assertThatThrownBy(() -> clubsServices.removeClub(club1.getId())).isInstanceOf(AccessDeniedException.class);

        verify(clubsRepository, never()).findById(anyInt());
        verify(clubsRepository, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeClubWhenAdminIsNotResponsible_shouldThrow() {

        when(currentUserService.getCurrentUser()).thenReturn(adminCurrentUser);

        when(clubsRepository.findById(club1.getId())).thenReturn(Optional.of(club1));

        assertThatThrownBy(() -> clubsServices.removeClub(club1.getId())).isInstanceOf(CustomException.InsufficientRoleException.class);

        verify(clubsRepository, never()).delete(any());
    }
}
