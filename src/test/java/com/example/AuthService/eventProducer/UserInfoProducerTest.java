package com.example.AuthService.eventProducer;

import com.example.AuthService.models.UserInfoDto;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"user_service"})
public class UserInfoProducerTest {

    @Autowired
    private UserInfoProducer userInfoProducer;

    @Value("${spring.kafka.topic-json.name}")
    private String topicName;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, UserInfoDto> consumer;

    @BeforeEach
    void setUp() {
        // Configure the consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create the consumer factory with proper deserializers
        DefaultKafkaConsumerFactory<String, UserInfoDto> cf = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(UserInfoDto.class, false));

        // Create the consumer
        consumer = cf.createConsumer();

        // Subscribe to the topic
        consumer.subscribe(Collections.singleton(topicName));

        // Clear any existing messages
        consumer.poll(Duration.ofMillis(100));
    }

    @AfterEach
    void tearDown() {
        // Close the consumer
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void testSendEventToKafka() {
        // Create a test UserInfoDto
        UserInfoDto userInfoDto = new UserInfoDto(
            "testuser",
            "password123",
            "Test",
            "User",
            "test@example.com",
            1234567890L
        );

        // Send the message
        userInfoProducer.sendEventToKafka(userInfoDto);

        // Poll for the message
        ConsumerRecords<String, UserInfoDto> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

        // Verify the message was received
        assertNotNull(records);
        assertEquals(1, records.count());

        // Get the record and verify its contents
        UserInfoDto receivedUserInfo = records.iterator().next().value();
        assertNotNull(receivedUserInfo);
        assertEquals(userInfoDto.username(), receivedUserInfo.username());
        assertEquals(userInfoDto.firstName(), receivedUserInfo.firstName());
        assertEquals(userInfoDto.lastName(), receivedUserInfo.lastName());
        assertEquals(userInfoDto.email(), receivedUserInfo.email());
        assertEquals(userInfoDto.phoneNumber(), receivedUserInfo.phoneNumber());

        System.out.println("[DEBUG_LOG] Successfully received message: " + receivedUserInfo);
    }
}
