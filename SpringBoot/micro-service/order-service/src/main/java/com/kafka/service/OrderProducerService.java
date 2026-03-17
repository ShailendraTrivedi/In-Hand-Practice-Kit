package com.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.model.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing orders to Kafka.
 */
@Service
@RequiredArgsConstructor // generates constructor for final fields
public class OrderProducerService {

    private static final Logger log = LoggerFactory.getLogger(OrderProducerService.class);

    // We'll use this topic consistently across the project
    private static final String ORDER_TOPIC = "orders";

    private final KafkaTemplate<String, String> kafkaTemplate;

    // Reuse Spring Boot's ObjectMapper bean for (de)serialization
    private final ObjectMapper objectMapper;

    /**
     * Publishes the given order to the Kafka 'orders' topic.
     *
     * @param orderRequest data received from the REST API
     */
    public void publishOrder(OrderRequest orderRequest) {
        try {
            String payload = objectMapper.writeValueAsString(orderRequest);
            String key = orderRequest.getOrderId();
    
            log.info("Publishing order to Kafka. Topic: {}, Key: {}, Payload: {}",
                    ORDER_TOPIC, key, payload);

            // Send asynchronously and attach a callback using CompletableFuture
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(ORDER_TOPIC, key, payload);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    // Failure case: message was not successfully written to Kafka
                    log.error("Failed to send message to Kafka", ex);
                } else if (result != null) {
                    // Success case: log metadata (offset, partition, etc.)
                    log.info("Successfully sent message to Kafka. Topic={}, Partition={}, Offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
    
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderRequest to JSON", e);
            throw new RuntimeException("Failed to serialize order", e);
        }
    }
}

