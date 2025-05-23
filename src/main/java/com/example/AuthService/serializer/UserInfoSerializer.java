package com.example.AuthService.serializer;

import com.example.AuthService.models.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class UserInfoSerializer implements Serializer<UserInfoDto> {

    @Override
    public byte[] serialize(String s, UserInfoDto userInfoDto) {
        byte val[] = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            val = objectMapper.writeValueAsString(userInfoDto).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }
}
