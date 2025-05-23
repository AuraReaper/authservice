package com.example.AuthService.controller;

import com.example.AuthService.entities.RefreshToken;
import com.example.AuthService.entities.UserInfo;
import com.example.AuthService.models.UserInfoDto;
import com.example.AuthService.service.JwtService;
import com.example.AuthService.service.RefreshTokenService;
import com.example.AuthService.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSignupSuccess() throws Exception {
        // Arrange
        UserInfoDto userInfoDto = new UserInfoDto(
            "testuser",
            "Password123!",
            "Test",
            "User",
            "test@example.com",
            1234567890L
        );

        RefreshToken refreshToken = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .expiryDate(Instant.now().plusMillis(1000 * 60 * 60))
            .build();

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.signature";

        when(userDetailsService.signupUser(any(UserInfoDto.class))).thenReturn(true);
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtService.GenerateToken(anyString())).thenReturn(jwtToken);

        // Act & Assert
        mockMvc.perform(post("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInfoDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", is(jwtToken)))
            .andExpect(jsonPath("$.token", is(refreshToken.getToken())));
    }

    @Test
    public void testSignupUserAlreadyExists() throws Exception {
        // Arrange
        UserInfoDto userInfoDto = new UserInfoDto(
            "existinguser",
            "Password123!",
            "Existing",
            "User",
            "existing@example.com",
            1234567890L
        );

        when(userDetailsService.signupUser(any(UserInfoDto.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInfoDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", is("User already exisits")));
    }

    @Test
    public void testSignupValidationError() throws Exception {
        // Arrange
        UserInfoDto userInfoDto = new UserInfoDto(
            "invaliduser",
            "weak",
            "Invalid",
            "User",
            "invalid@example.com",
            1234567890L
        );

        when(userDetailsService.signupUser(any(UserInfoDto.class)))
            .thenThrow(new IllegalArgumentException("Password must be at least 8 characters long"));

        // Act & Assert
        mockMvc.perform(post("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInfoDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", is("Password must be at least 8 characters long")));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        // Arrange
        UserInfo user1 = UserInfo.builder()
            .userId(UUID.randomUUID().toString())
            .username("user1")
            .firstName("User")
            .lastName("One")
            .email("user1@example.com")
            .phoneNumber(1234567890L)
            .roles(new HashSet<>())
            .build();

        UserInfo user2 = UserInfo.builder()
            .userId(UUID.randomUUID().toString())
            .username("user2")
            .firstName("User")
            .lastName("Two")
            .email("user2@example.com")
            .phoneNumber(9876543210L)
            .roles(new HashSet<>())
            .build();

        List<UserInfo> users = Arrays.asList(user1, user2);

        when(userDetailsService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/auth/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].username", is("user1")))
            .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    public void testGetAllUsersError() throws Exception {
        // Arrange
        when(userDetailsService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/auth/v1/users"))
            .andExpect(status().isInternalServerError());
    }
}