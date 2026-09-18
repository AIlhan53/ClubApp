package com.example.clubappv1.config;

import com.example.clubappv1.services.UsersServices;
import com.example.clubappv1.utils.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT token filter for validating and processing JWTs in incoming HTTP requests.
 * This filter intercepts all requests and performs the following:
 * <ul>
 *     <li>Allows public paths to bypass JWT validation</li>
 *     <li>Validates JWT from the "Authorization" header for secured endpoints</li>
 *     <li>Extracts the user from the token and sets the authentication in the SecurityContext</li>
 *     <li>Ensures the filter chain continues regardless of token presence or validation</li>
 * </ul>
 */
@Component
public class    JWTTokenFilter extends OncePerRequestFilter {

    /**
     * Utility class for JWT operations (generate, validate, extract claims, etc.).
     */

    private final JWTUtils jWTUtils;

    /**
     * User service used to load user details for authentication.
     */
    private final UsersServices service;

    public JWTTokenFilter(JWTUtils jWTUtils, UsersServices service) {
        this.jWTUtils = jWTUtils;
        this.service = service;
    }

    /**
     * Filters each incoming request to process JWT authentication.
     * Steps performed:
     * <ol>
     *     <li>Allows OPTIONS requests to pass through for CORS preflight</li>
     *     <li>Skips JWT validation for public endpoints</li>
     *     <li>Checks the "Authorization" header for a valid Bearer token</li>
     *     <li>Validates the token and extracts the username</li>
     *     <li>Loads user details and sets the authentication in the SecurityContext</li>
     *     <li>Continues the filter chain regardless of success or failure</li>
     * </ol>
     *
     * @param request the incoming HttpServletRequest
     * @param response the outgoing HttpServletResponse
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println(">>> JWTTokenFilter.doFilterInternal: " + request.getRequestURI());

        // Handle OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        var path = request.getRequestURI();

        // Public paths that do not require JWT authentication
        if (path.startsWith("/auth/")
                || path.startsWith("/tournaments/invitation/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/error")) {
            System.out.println("Public path, skipping JWT check: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // JWT validation
        String header = request.getHeader("Authorization");
        System.out.println("HEADER AUTH = " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("No valid Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            String email = jWTUtils.getUsernameFromToken(token);
            System.out.println("Email from token: " + email);

            var user = service.loadUserByUsername(email);
            System.out.println("User loaded: " + user.getUsername() + " with roles: " + user.getAuthorities());

            if (!jWTUtils.isTokenValid(token,user)) {
                System.out.println("Token validation failed");
                filterChain.doFilter(request, response);
                return;
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Authentication set in SecurityContext");

        } catch (Exception e) {
            logger.warn("Échec de traitement du token JWT");
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
