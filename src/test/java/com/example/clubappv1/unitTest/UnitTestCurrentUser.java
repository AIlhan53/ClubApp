package com.example.clubappv1.unitTest;

import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.models.*;
import com.example.clubappv1.repositories.UsersRepository;
import com.example.clubappv1.services.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitTestCurrentUser {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    private Users existingUser;
    private Users adminUser;
    private Users responsableUser;
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

        responsableUser = new Users();
        responsableUser.setId(20);
        responsableUser.setEmail("responsable@mail.com");
        responsableUser.setFirstName("responsable");
        responsableUser.setLastName("User");
        responsableUser.setPassword("hashedPassword789");
        responsableUser.setRole(responsableRole);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void getCurrentUser_shouldThrow_whenUserNotFound() {

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);

        when(authentication.getName()).thenReturn("unexist");

        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(usersRepository.findUserByEmail("unexist"))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> currentUserService.getCurrentUser());
    }

}
