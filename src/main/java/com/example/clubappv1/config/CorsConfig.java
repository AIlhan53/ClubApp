package com.example.clubappv1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS).
 * This class defines the CORS policy for the application, specifying:
 * <ul>
 *     <li>Allowed origins (frontend domains)</li>
 *     <li>Permitted HTTP methods</li>
 *     <li>Allowed and exposed headers</li>
 *     <li>Whether credentials (cookies, authorization headers) are allowed</li>
 *     <li>Max age for preflight requests</li>
 * </ul>
 */
@Configuration
public class CorsConfig {

    /**
     * Creates a CorsConfigurationSource bean to define the application's CORS settings.
     * This configuration allows:
     * <ul>
     *     <li>Origins: https://localhost:4200, https://localhost:8443</li>
     *     <li>HTTP methods: GET, POST, PUT, DELETE, OPTIONS, PATCH</li>
     *     <li>Allowed headers: Authorization, Content-Type, Accept, Origin, X-Requested-With</li>
     *     <li>Exposed headers: Authorization, Content-Type</li>
     *     <li>Credentials: allowed</li>
     *     <li>Max age for preflight requests: 3600 seconds</li>
     * </ul>
     *
     * @return a configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        configuration.setAllowedOrigins(Arrays.asList(
                "https://localhost:4200",
                "https://localhost:8443"
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Exposed headers (readable by the frontend)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Max age for preflight requests (in seconds)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
