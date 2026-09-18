package com.example.clubappv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the ClubApp Spring Boot application.
 *
 * <p>
 * This class bootstraps the application context and enables scheduled
 * tasks throughout the application via {@code @EnableScheduling}.
 * </p>
 */
@EnableScheduling
@SpringBootApplication
public class ClubAppV1Application {

    /**
     * Starts the ClubApp application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ClubAppV1Application.class, args);
    }

}
