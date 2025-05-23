package com.example.AuthService.serializer;

import com.example.AuthService.eventProducer.UserInfoEvent;
import com.example.AuthService.models.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class UserInfoSerializer implements Serializer<UserInfoEvent> {

    @Override
    public byte[] serialize(String s, UserInfoEvent userInfoEvent) {
        byte[] val = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            val = objectMapper.writeValueAsString(userInfoEvent).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }
}
