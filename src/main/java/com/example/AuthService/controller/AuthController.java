package com.example.AuthService.controller;

import com.example.AuthService.auth.JwtAuthFilter;
import com.example.AuthService.entities.RefreshToken;
import com.example.AuthService.models.UserInfoDto;
import com.example.AuthService.response.JwtResponseDto;
import com.example.AuthService.service.JwtService;
import com.example.AuthService.service.RefreshTokenService;
import com.example.AuthService.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class AuthController {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @PostMapping("auth/v1/signup")
    public ResponseEntity signup(@RequestBody UserInfoDto userInfoDto){
        try {
            Boolean isSignuped = userDetailsService.signupUser(userInfoDto);
            if(Boolean.FALSE.equals(isSignuped)){
                return new ResponseEntity<>("User already exisits", HttpStatus.BAD_REQUEST);
            }
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUserName());
            String jwtToken = jwtService.GenerateToken(userInfoDto.getUserName());
            return new ResponseEntity<>(JwtResponseDto.builder()
                    .accessToken(jwtToken)
                    .token(refreshToken.getToken())
                    .build(), HttpStatus.OK
            );
        } catch(Exception e) {
            return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
