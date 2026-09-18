package com.example.clubappv1.services;

import com.example.clubappv1.Exception.CustomException;
import com.example.clubappv1.dto.RegisterRequest;
import com.example.clubappv1.dto.UserResponse;
import com.example.clubappv1.mapper.UserMapper;
import com.example.clubappv1.models.RoleType;
import com.example.clubappv1.models.Roles;
import com.example.clubappv1.models.Users;
import com.example.clubappv1.models.VerificationToken;
import com.example.clubappv1.repositories.RolesRepository;
import com.example.clubappv1.repositories.UsersRepository;
import com.example.clubappv1.utils.JWTUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
/**
 * Service handling authentication and account lifecycle operations.
 *
 * <p>Provides user login (JWT token generation), registration with
 * email-based account activation, and activation token verification.</p>
 */
@Service
public class AuthServices {

    private final AuthenticationManager authenticationManager;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final VerificationTokenService tokenService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;

    public AuthServices(
            AuthenticationManager authenticationManager,
            UsersRepository usersRepository,
            RolesRepository rolesRepository,
            VerificationTokenService tokenService,
            MailService mailService,
            JWTUtils jwtUtils,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.mailService = mailService;
    }

    /**
     * Authenticates a user with their email and password.
     *
     * <p>On success, sets the resulting authentication in the security
     * context and generates a new access token and refresh token.</p>
     *
     * @param email    the user's email address
     * @param password the user's raw (unencoded) password
     * @return a map containing the generated {@code accessToken} and {@code refreshToken}
     */
    public Map<String, String> login(String email, String password) {
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
        var user = (User) auth.getPrincipal();
        return Map.of(
                "accessToken", jwtUtils.createToken(user),
                "refreshToken", jwtUtils.createRefreshToken(user)
        );
    }

    /**
     * Registers a new user account.
     *
     * <p>The account is created inactive and must be activated via the
     * link sent by email before it can be used to log in. Registering
     * an account with the {@code ADMIN} role is not permitted through
     * this endpoint.</p>
     *
     * @param request the registration details of the new user
     * @return the created user, mapped to a response DTO
     * @throws CustomException.AlreadyDataException if an account already exists for the given email
     * @throws CustomException.InsufficientRoleException if the requested role is {@code ADMIN}
     */
    @Transactional
    public UserResponse register(RegisterRequest request) throws CustomException.AlreadyDataException {

        String userEmail = request.getEmail().trim();

        // Pour éviter la récurrence et les espaces inutiles
        if (usersRepository.findByEmailIgnoreCase(userEmail).isPresent()) {
            throw new CustomException.AlreadyDataException("This email already exists");
        }

        Roles role = rolesRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

        if (request.getRole() == RoleType.ADMIN) {
            throw new CustomException.InsufficientRoleException("You are not allowed to perform this action");
        }

        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);
        user.setRole(role);

        Users savedUser = usersRepository.save(user);

        VerificationToken token = tokenService.createTokenForUser(savedUser);

        // envoyer l'email
        try {
            mailService.sendActivationEmail(savedUser.getEmail(), savedUser.getFirstName(), token.getToken());
        } catch (Exception e) {
            System.err.println("Erreur envoi email : " + e.getMessage());
        }
        return UserMapper.toResponse(savedUser);
    }

    /**
     * Activates a user account using its verification token.
     *
     * <p>The token must exist and not be expired. Once used, the token
     * is deleted so it cannot be reused.</p>
     *
     * @param token the activation token sent to the user by email
     * @throws NoSuchElementException if no verification token matches the given value
     * @throws CustomException.InvalidTokenException if the token has expired
     */
    @Transactional
    public void activateAccount(String token) {

        VerificationToken verificationToken = tokenService.getByToken(token)
                .orElseThrow(() ->
                        new NoSuchElementException("Invalid activation token")
                );

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenService.deleteToken(verificationToken);

            throw new CustomException.InvalidTokenException(
                    "Activation token has expired"
            );
        }

        Users user = verificationToken.getUser();

        user.setActive(true);
        usersRepository.save(user);

        // Le token ne peut être utilisé qu'une seule fois
        tokenService.deleteToken(verificationToken);
    }
}