package com.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Simple DTO representing an incoming order request from the client.
 *
 * In a real system, this could be much richer (address, items, etc.).
 * We keep it small but realistic.
 */
@Data // generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // needed for JSON deserialization
@AllArgsConstructor // convenient constructor for manual creation/tests
public class OrderRequest {

    // Unique id of the order, used as Kafka message key as well
    private String orderId;

    // Simple customer identifier
    private String customerId;

    // Order total amount
    private BigDecimal amount;

    // Currency code, e.g. "USD"
    private String currency;
}

