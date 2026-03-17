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
 * Core order-processing consumer.
 *
 * Listens to the "orders" topic and performs main business logic for each order.
 */
@Service
@RequiredArgsConstructor
public class OrderConsumerService {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumerService.class);

    // Shared ObjectMapper bean injected by Spring
    private final ObjectMapper objectMapper;

    /**
     * Kafka listener that consumes messages from the "orders" topic.
     * Payload arrives as JSON string and is converted to OrderRequest.
     */
    @KafkaListener(
            topics = "orders",
            groupId = "order-processing-group"
    )
    public void onOrderMessage(String message) {
        log.info("Received message from Kafka: {}", message);

        try {
            OrderRequest order = objectMapper.readValue(message, OrderRequest.class);
            processOrder(order);
        } catch (JsonProcessingException e) {
            // Serialization/format errors are logged; error handling policies can be added later
            log.error("Failed to deserialize Kafka message to OrderRequest", e);
        }
    }

    /**
     * Simulated business logic for processing an order.
     * In a real application this might:
     *  - validate the order
     *  - update inventory
     *  - call payment service
     *  - persist to database, etc.
     */
    private void processOrder(OrderRequest order) {
        log.info(
                "Processing order. ID={}, CustomerId={}, Amount={}, Currency={}",
                order.getOrderId(),
                order.getCustomerId(),
                order.getAmount(),
                order.getCurrency()
        );
    }
}