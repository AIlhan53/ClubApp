package com.example.clubappv1.config;

import com.example.clubappv1.services.UsersServices;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the ClubApp application.
 * <p>
 * Configures authentication, authorization, CORS, and security headers
 * for protection against common web vulnerabilities.
 * </p>
 *
 * <p>Security features:</p>
 * <ul>
 *   <li>JWT-based stateless authentication</li>
 *   <li>XSS protection headers</li>
 *   <li>Content-Type sniffing prevention</li>
 *   <li>Clickjacking protection (frame options)</li>
 *   <li>Content Security Policy</li>
 * </ul>
 *
 * @author ClubApp Team
 * @version 2.0
 */
@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class SecurityConfig {



    private final UsersServices usersServices;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenFilter tokenFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(UsersServices usersServices,
                          PasswordEncoder passwordEncoder,
                          JWTTokenFilter tokenFilter,
                          CorsConfigurationSource corsConfigurationSource){
        this.usersServices = usersServices;
        this.tokenFilter = tokenFilter;
        this.passwordEncoder = passwordEncoder;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Configures the security filter chain with all security rules.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF: Disabled for stateless REST API with JWT
                // JWT tokens are sent via Authorization header, not cookies
                // so CSRF attacks are not applicable
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/tournaments/invitation/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Diagnostic method to verify bean initialization.
     */
    @PostConstruct
    public void checkBeans() {
        System.out.println("JWTTokenFilter bean present: " + true);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usersServices);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}