package org.example.notifyservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.notifyservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class BookingCreatedConsumer {

    private final EmailService emailService;

    public BookingCreatedConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${notification.kafka.booking-created-topic}")
    public void consume(String email) {
        log.info("Receisvesd message to send email to :{}",email);
        emailService.sendBookingCreatedEmail(email);
    }
}
