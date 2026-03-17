package com.kafka.controller;

import com.kafka.model.OrderRequest;
import com.kafka.service.OrderProducerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes endpoints for creating orders.
 *
 * When a new order is received, it is published to Kafka.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor // generates constructor for final fields (orderProducerService)
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    // Service responsible for sending orders to Kafka
    private final OrderProducerService orderProducerService;

    /**
     * Create a new order and publish it to Kafka.
     *
     * Example request (JSON):
     * {
     *   "orderId": "ORD-1001",
     *   "customerId": "CUST-2001",
     *   "amount": 149.99,
     *   "currency": "USD"
     * }
     */
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Received new order via REST API: {}", orderRequest.getOrderId());

        // Delegate to producer service; this will serialize and push to Kafka
        orderProducerService.publishOrder(orderRequest);

        // Return a simple confirmation to the client
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Order accepted and sent to Kafka. Order ID: " + orderRequest.getOrderId());
    }
}

