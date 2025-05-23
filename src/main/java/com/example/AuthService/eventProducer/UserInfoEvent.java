package com.example.AuthService.eventProducer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInfoEvent(
         String firstName,
         String lastName,
         String email,
         Long phoneNumber,
         String userId
) {
}
