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
 * Simulates a payment service that charges the customer for each order.
 */
@Service
@RequiredArgsConstructor
public class PaymentConsumerService {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumerService.class);

    // Shared ObjectMapper for JSON deserialization
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "orders",
            groupId = "payment-service-group"  // separate group -> independent subscriber
    )
    public void onOrderForPayment(String message) {
        log.info("[Payment] Received order event: {}", message);

        try {
            OrderRequest order = objectMapper.readValue(message, OrderRequest.class);
            processPayment(order);
        } catch (JsonProcessingException e) {
            log.error("[Payment] Failed to deserialize message", e);
        }
    }

    private void processPayment(OrderRequest order) {
        // Simulate charging the customer
        log.info(
                "[Payment] Processing payment of {} {} for order {} (customer {})",
                order.getAmount(),
                order.getCurrency(),
                order.getOrderId(),
                order.getCustomerId()
        );
    }
}