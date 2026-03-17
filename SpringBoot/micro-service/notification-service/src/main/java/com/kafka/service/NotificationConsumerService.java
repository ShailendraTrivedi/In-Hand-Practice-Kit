package com.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.model.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Simulates a notification service that reacts to new orders.
 * In a real app this might send emails, SMS, push notifications, etc.
 */
@Service
@RequiredArgsConstructor
public class NotificationConsumerService {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumerService.class);

    // Shared ObjectMapper for JSON deserialization
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "orders",
            groupId = "notification-service-group"  // separate group -> independent subscriber
    )
    public void onOrderForNotification(String message) {
        log.info("[Notification] Received order event: {}", message);

        try {
            OrderRequest order = objectMapper.readValue(message, OrderRequest.class);
            sendNotification(order);
        } catch (JsonProcessingException e) {
            log.error("[Notification] Failed to deserialize message", e);
        }
    }

    private void sendNotification(OrderRequest order) {
        // Simulate sending a notification
        log.info(
                "[Notification] Sending notification for order {} to customer {}",
                order.getOrderId(),
                order.getCustomerId()
        );
    }
}