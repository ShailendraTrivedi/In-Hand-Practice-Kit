package com.e_commerce.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Order {
    private Long id;
    private Long productId;
    private Integer quantity;
    private OrderStatus status = OrderStatus.PENDING;
    private Double totalAmount;
    private String idempotencyKey;
    
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}

