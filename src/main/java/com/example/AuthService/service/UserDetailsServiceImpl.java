package com.example.AuthService.service;

import com.example.AuthService.entities.UserInfo;
import com.example.AuthService.eventProducer.UserInfoEvent;
import com.example.AuthService.eventProducer.UserInfoProducer;
import com.example.AuthService.models.UserInfoDto;
import com.example.AuthService.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
@Data
@Slf4j
@Builder
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;
    private final UserInfoProducer userInfoProducer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userRepository.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user);
    }

    public UserInfo checkIfUserAlreadyExsist(UserInfoDto userInfoDto){
        return userRepository.findByUsername(userInfoDto.username());
    }

    public Boolean signupUser(UserInfoDto userInfoDto){
        String validationError = userValidationService.validte(userInfoDto.email(), userInfoDto.password());
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        String encodedPassword = passwordEncoder.encode(userInfoDto.password());

        if (Objects.nonNull(checkIfUserAlreadyExsist(userInfoDto))) {
            return false;
        }

        String userId = UUID.randomUUID().toString();
        UserInfo user = UserInfo.builder()
                .userId(userId)
                .username(userInfoDto.username())
                .password(encodedPassword)
                .firstName(userInfoDto.firstName())
                .lastName(userInfoDto.lastName())
                .email(userInfoDto.email())
                .phoneNumber(userInfoDto.phoneNumber())
                .roles(new HashSet<>())
                .build();
        userRepository.save(user);
        // push event to queue
        userInfoProducer.sendEventToKafka(userInfoEventToPublish(userInfoDto, userId));
        return true;
    }

    public List<UserInfo> getAllUsers() {
        return (List<UserInfo>) userRepository.findAll();
    }

    private UserInfoEvent userInfoEventToPublish(UserInfoDto userInfoDto, String userId) {
        return UserInfoEvent.builder()
                .userId(userId)
                .firstName(userInfoDto.firstName())
                .lastName(userInfoDto.lastName())
                .email(userInfoDto.email())
                .phoneNumber(userInfoDto.phoneNumber())
                .build();
    }
}
