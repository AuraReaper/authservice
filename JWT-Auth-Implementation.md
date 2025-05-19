# JWT Authentication Service Implementation

## Overview
This document provides a brief explanation of how the authentication service is implemented using JWT (JSON Web Tokens) in this Spring Boot application.

## Architecture

The authentication service is built using:
- **Spring Boot** as the application framework
- **Spring Security** for authentication and authorization
- **JWT (JSON Web Tokens)** for secure token-based authentication
- **JPA/Hibernate** for database operations
- **MySQL** as the database

## Key Components

### Entity Classes
1. **UserInfo**: Represents user data with fields for userId, username, password, and roles
2. **UserRoles**: Represents the roles assigned to users
3. **RefreshToken**: Stores refresh tokens with expiry dates for extending sessions

### Repository Interfaces
1. **UserRepository**: Provides data access methods for user information
2. **RefreshTokenRepository**: Handles persistence of refresh tokens

### Service Classes
1. **JwtService**: Handles JWT token creation, validation, and parsing
2. **RefreshTokenService**: Manages refresh token creation and verification
3. **UserDetailsServiceImpl**: Implements Spring Security's UserDetailsService
4. **CustomUserDetails**: Adapts our user model to Spring Security's UserDetails

## JWT Implementation

### Token Generation
When a user successfully authenticates:
1. The system validates credentials using `UserDetailsServiceImpl`
2. `JwtService.createToken()` generates a JWT token containing:
   - User claims (subject/username)
   - Issuance timestamp
   - Expiration time (set to 1 minute in this implementation)
   - Digital signature using HS256 algorithm with a secret key

### Token Validation
For protected endpoints:
1. The JWT token is extracted from the Authorization header
2. `JwtService.validateToken()` verifies:
   - Token signature using the secret key
   - Token expiration status
   - Username in the token matches the authenticated user

### Refresh Token Mechanism
To extend sessions without requiring re-authentication:
1. `RefreshTokenService.createRefreshToken()` generates a refresh token with:
   - A UUID as the token value
   - Association with a specific user
   - Expiration time (set to 1 minute in this implementation)
2. When the JWT token expires, the client can use the refresh token to request a new JWT
3. `RefreshTokenService.verifyRefreshToken()` validates the refresh token before issuing a new JWT

## Security Flow
1. User provides credentials (username/password)
2. System authenticates and returns both a JWT and a refresh token
3. Client includes JWT in Authorization header for subsequent requests
4. When JWT expires, client uses refresh token to obtain a new JWT
5. If refresh token expires, user must re-authenticate with credentials

This implementation provides secure, stateless authentication while allowing for session extension through the refresh token mechanism.