package com.example.AuthService.eventProducer;

import com.example.AuthService.models.UserInfoDto;
import org.springframework.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoProducer {

    private final KafkaTemplate<String, UserInfoDto> kafkaTemplate;

    @Value("${spring.kafka.topic-json.name}")
    private String topicJsonName;

    public void sendEventToKafka(UserInfoDto eventData) {
        Message<UserInfoDto> message = MessageBuilder.withPayload(eventData)
                .setHeader(KafkaHeaders.TOPIC, topicJsonName)
                .build();
        kafkaTemplate.send(message);
    }
}
