package com.example.AuthService.service;

import com.example.AuthService.entities.RefreshToken;
import com.example.AuthService.entities.UserInfo;
import com.example.AuthService.repository.RefreshTokenRepository;
import com.example.AuthService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UserInfo testUser;
    private RefreshToken validRefreshToken;
    private RefreshToken expiredRefreshToken;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = UserInfo.builder()
                .userId(UUID.randomUUID().toString())
                .username("testuser")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phoneNumber(1234567890L)
                .roles(new HashSet<>())
                .build();

        // Create a valid refresh token
        validRefreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userInfo(testUser)
                .expiryDate(Instant.now().plusMillis(1000 * 60 * 60)) // 1 hour in the future
                .build();

        // Create an expired refresh token
        expiredRefreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userInfo(testUser)
                .expiryDate(Instant.now().minusMillis(1000 * 60 * 60)) // 1 hour in the past
                .build();
    }

    @Test
    void testCreateRefreshToken() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUserInfo());
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
        
        verify(userRepository).findByUsername("testuser");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        
        System.out.println("[DEBUG_LOG] Created refresh token: " + result.getToken());
    }

    @Test
    void testVerifyRefreshToken_Valid() {
        // Act
        RefreshToken result = refreshTokenService.verifyRefreshToken(validRefreshToken);

        // Assert
        assertEquals(validRefreshToken, result);
        System.out.println("[DEBUG_LOG] Verified valid refresh token: " + result.getToken());
    }

    @Test
    void testVerifyRefreshToken_Expired() {
        // Arrange
        doNothing().when(refreshTokenRepository).delete(expiredRefreshToken);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.verifyRefreshToken(expiredRefreshToken);
        });
        
        assertTrue(exception.getMessage().contains("has expired"));
        verify(refreshTokenRepository).delete(expiredRefreshToken);
        
        System.out.println("[DEBUG_LOG] Exception when verifying expired token: " + exception.getMessage());
    }

    @Test
    void testFindByToken() {
        // Arrange
        String tokenValue = validRefreshToken.getToken();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(validRefreshToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenValue);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validRefreshToken, result.get());
        verify(refreshTokenRepository).findByToken(tokenValue);
        
        System.out.println("[DEBUG_LOG] Found refresh token: " + result.get().getToken());
    }

    @Test
    void testFindByToken_NotFound() {
        // Arrange
        String nonExistentToken = "non-existent-token";
        when(refreshTokenRepository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken(nonExistentToken);

        // Assert
        assertFalse(result.isPresent());
        verify(refreshTokenRepository).findByToken(nonExistentToken);
        
        System.out.println("[DEBUG_LOG] Token not found: " + nonExistentToken);
    }
}