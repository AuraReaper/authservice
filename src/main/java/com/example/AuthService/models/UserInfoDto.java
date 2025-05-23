package com.example.AuthService.models;

public record UserInfoDto(String username,
                          String password,
                          String firstName,
                          String lastName,
                          String email,
                          Long phoneNumber
){

}
