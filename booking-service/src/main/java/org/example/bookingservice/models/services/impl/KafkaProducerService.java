package org.example.bookingservice.models.services.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${notification.kafka.booking-created-topic:booking-created}")
    private String topicName;

    public void sendMessage(String payload){
        kafkaTemplate.send(topicName,payload);
    }
}
