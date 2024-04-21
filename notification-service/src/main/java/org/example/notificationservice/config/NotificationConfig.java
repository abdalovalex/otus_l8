package org.example.notificationservice.config;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.event.OrderTransactionApprovedEvent;
import org.example.notificationservice.event.OrderTransactionRejectedEvent;
import org.example.notificationservice.service.NotificationService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class NotificationConfig {
    private final NotificationService notificationService;

    @Bean
    public Consumer<OrderTransactionApprovedEvent> positive() {
        return notificationService::sendPositive;
    }

    @Bean
    public Consumer<OrderTransactionRejectedEvent> negative() {
        return notificationService::sendNegative;
    }
}
