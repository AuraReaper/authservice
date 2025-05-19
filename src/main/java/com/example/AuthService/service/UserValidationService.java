package com.example.AuthService.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {

    private final PasswordEncoder passwordEncoder;

    public UserValidationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

   public String validte(String email, String password) {
       if (!EMAIL_PATTERN.matcher(email).matches()) {
           return "Invalid email format";
       }
       if (password.length() < 8) {
           return "Password must be at least 8 characters long";
       }
       return null;
   }
}
