package com.example.AuthService.service;

import com.example.AuthService.entities.UserInfo;
import com.example.AuthService.eventProducer.UserInfoProducer;
import com.example.AuthService.models.UserInfoDto;
import com.example.AuthService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserInfoProducer userInfoProducer;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UserInfo testUser;
    private UserInfoDto userInfoDto;

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

        // Create a test UserInfoDto
        userInfoDto = new UserInfoDto(
                "testuser",
                "password123",
                "Test",
                "User",
                "test@example.com",
                1234567890L
        );
    }

    @Test
    void testLoadUserByUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        verify(userRepository).findByUsername("testuser");
        
        System.out.println("[DEBUG_LOG] Loaded user: " + userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(null);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistentuser");
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("nonexistentuser");
        
        System.out.println("[DEBUG_LOG] Exception when loading non-existent user: " + exception.getMessage());
    }

    @Test
    void testCheckIfUserAlreadyExsist() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        UserInfo result = userDetailsService.checkIfUserAlreadyExsist(userInfoDto);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findByUsername("testuser");
        
        System.out.println("[DEBUG_LOG] Checked if user exists: " + result.getUsername());
    }

    @Test
    void testSignupUser_Success() {
        // Arrange
        when(userValidationService.validte(anyString(), anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRepository.save(any(UserInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(userInfoProducer).sendEventToKafka(any(UserInfoDto.class));

        // Act
        Boolean result = userDetailsService.signupUser(userInfoDto);

        // Assert
        assertTrue(result);
        verify(userValidationService).validte(userInfoDto.email(), userInfoDto.password());
        verify(passwordEncoder).encode(userInfoDto.password());
        verify(userRepository).findByUsername(userInfoDto.username());
        verify(userRepository).save(any(UserInfo.class));
        verify(userInfoProducer).sendEventToKafka(any(UserInfoDto.class));
        
        System.out.println("[DEBUG_LOG] User signup result: " + result);
    }

    @Test
    void testSignupUser_UserAlreadyExists() {
        // Arrange
        when(userValidationService.validte(anyString(), anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        // Act
        Boolean result = userDetailsService.signupUser(userInfoDto);

        // Assert
        assertFalse(result);
        verify(userValidationService).validte(userInfoDto.email(), userInfoDto.password());
        verify(passwordEncoder).encode(userInfoDto.password());
        verify(userRepository).findByUsername(userInfoDto.username());
        verify(userRepository, never()).save(any(UserInfo.class));
        verify(userInfoProducer, never()).sendEventToKafka(any(UserInfoDto.class));
        
        System.out.println("[DEBUG_LOG] User already exists, signup result: " + result);
    }

    @Test
    void testSignupUser_ValidationError() {
        // Arrange
        when(userValidationService.validte(anyString(), anyString())).thenReturn("Invalid email format");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userDetailsService.signupUser(userInfoDto);
        });
        
        assertEquals("Invalid email format", exception.getMessage());
        verify(userValidationService).validte(userInfoDto.email(), userInfoDto.password());
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any(UserInfo.class));
        verify(userInfoProducer, never()).sendEventToKafka(any(UserInfoDto.class));
        
        System.out.println("[DEBUG_LOG] Validation error during signup: " + exception.getMessage());
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        UserInfo user1 = testUser;
        UserInfo user2 = UserInfo.builder()
                .userId(UUID.randomUUID().toString())
                .username("anotheruser")
                .password("encodedPassword")
                .firstName("Another")
                .lastName("User")
                .email("another@example.com")
                .phoneNumber(9876543210L)
                .roles(new HashSet<>())
                .build();
        
        List<UserInfo> userList = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(userList);

        // Act
        List<UserInfo> result = userDetailsService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals(user1, result.get(0));
        assertEquals(user2, result.get(1));
        verify(userRepository).findAll();
        
        System.out.println("[DEBUG_LOG] Retrieved " + result.size() + " users");
    }
}