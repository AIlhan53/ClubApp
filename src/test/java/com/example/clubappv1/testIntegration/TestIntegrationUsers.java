package com.example.clubappv1.testIntegration;

import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.RoleType;
import com.example.clubappv1.models.Roles;
import com.example.clubappv1.models.Users;
import com.example.clubappv1.repositories.UsersRepository;
import com.example.clubappv1.services.CurrentUserService;
import com.example.clubappv1.services.UsersServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TestIntegrationUsers {

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private CurrentUserService currentUserService;

    @Autowired
    private UsersServices usersServices;

    private Users existingUser;
    private Users adminUser;
    private Roles memberRole;
    private Roles adminRole;
    private Roles responsableRole;

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


    @Test
    @WithMockUser(roles = "ADMIN")
    void removeUserWhenAdmin_shouldBeAllowed() {

        when(currentUserService.getCurrentUser())
                .thenReturn(adminCurrentUser);

        when(usersRepository.findById(1))
                .thenReturn(Optional.of(existingUser));

        usersServices.removeUser(1);

        verify(usersRepository).delete(existingUser);
    }

    @Test
    @WithMockUser(roles = "MEMBRE")
    void removeUserWhenNotAdmin_shouldThrow() {

        assertThatThrownBy(() -> usersServices.removeUser(1))
                .isInstanceOf(AccessDeniedException.class);

        verify(usersRepository, never()).findById(anyInt());
        verify(usersRepository, never()).delete(any());
    }
}
