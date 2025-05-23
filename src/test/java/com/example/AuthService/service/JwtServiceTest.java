package com.example.AuthService.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private final String username = "testuser";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        userDetails = new User(username, "password", new ArrayList<>());
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtService.GenerateToken(username);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        System.out.println("[DEBUG_LOG] Generated token: " + token);
    }

    @Test
    void testExtractUsername() {
        // Arrange
        String token = jwtService.GenerateToken(username);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername);
        System.out.println("[DEBUG_LOG] Extracted username: " + extractedUsername);
    }

    @Test
    void testValidateToken() {
        // Arrange
        String token = jwtService.GenerateToken(username);

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
        System.out.println("[DEBUG_LOG] Token validation result: " + isValid);
    }

    @Test
    void testValidateTokenWithWrongUsername() {
        // Arrange
        String token = jwtService.GenerateToken(username);
        UserDetails wrongUser = new User("wronguser", "password", new ArrayList<>());

        // Act
        boolean isValid = jwtService.validateToken(token, wrongUser);

        // Assert
        assertFalse(isValid);
        System.out.println("[DEBUG_LOG] Token validation with wrong username result: " + isValid);
    }

    @Test
    void testExtractExpiration() {
        // Arrange
        String token = jwtService.GenerateToken(username);

        // Act
        Date expirationDate = jwtService.extractExpiration(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
        System.out.println("[DEBUG_LOG] Expiration date: " + expirationDate);
    }

    @Test
    void testExtractClaim() {
        // Arrange
        String token = jwtService.GenerateToken(username);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals(username, subject);
        System.out.println("[DEBUG_LOG] Extracted subject: " + subject);
    }
}