package com.example.clubappv1.utils;

import com.example.clubappv1.services.UsersServices;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for handling JSON Web Token (JWT) creation and validation.
 *
 * <p>
 * This class is responsible for generating access tokens and refresh tokens,
 * extracting information from tokens, and validating their integrity.
 * </p>
 *
 * <p>
 * Tokens are signed using the HS512 algorithm and a secret key defined
 * in the application configuration.
 * </p>
 */
@Component
public class JWTUtils {

    private final UsersServices usersServices;



    /**
     * Secret key used to sign and verify JWT tokens.
     */
    private final SecretKey key;
    public JWTUtils(UsersServices usersServices, @Value("${jwt.secret}") String secret) {
        this.usersServices = usersServices;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Expiration time (in milliseconds) for access tokens.
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Expiration time (in milliseconds) for refresh tokens.
     */
    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;

    /**
     * Generates a JWT access token for an authenticated user.
     * <p>
     * The token contains:
     * <ul>
     *     <li>The user's email as the subject</li>
     *     <li>The issued date</li>
     *     <li>The expiration date</li>
     *     <li>The user's roles as custom claims</li>
     * </ul>
     *
     * @param user the authenticated Spring Security user
     * @return a signed JWT access token
     */
    public String createToken(UserDetails user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer("clubapp")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    /**
     * Generates a JWT refresh token for an authenticated user.
     *
     * <p>
     * The refresh token has a longer validity period than the access token
     * and is typically used to obtain a new access token without
     * re-authenticating.
     * </p>
     *
     * @param user the authenticated Spring Security user
     * @return a signed JWT refresh token
     */
    public String createRefreshToken(User user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer("clubapp")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }


    /**
     * Generates a JWT refresh token for an authenticated user.
     *
     * <p>
     * The refresh token has a longer validity period than the access token
     * and is typically used to obtain a new access token without
     * re-authenticating.
     * </p>
     *
     * @param refreshToken the token
     * @return a signed JWT refresh token
     */
    public String refreshAccessToken(String refreshToken) {
        String username = getUsernameFromToken(refreshToken);
        UserDetails user = usersServices.loadUserByUsername(username);
        return createToken((User) user);
    }


    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates a JWT token.
     * <p>
     * This method checks:
     * <ul>
     *     <li>The token signature</li>
     *     <li>The token structure</li>
     *     <li>The expiration date</li>
     * </ul>
     *
     * @param token the JWT token to validate
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiration.before(new Date());
    }

}
