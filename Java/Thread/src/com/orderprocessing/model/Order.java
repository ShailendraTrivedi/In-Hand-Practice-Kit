package com.orderprocessing.model;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an order in the system.
 * Uses thread-safe ID generation with AtomicLong.
 */
public class Order {
    private static final AtomicLong orderIdCounter = new AtomicLong(1);
    
    private final String orderId;
    private final String userId;
    private final String productId;
    private final int quantity;
    private final double price;
    private volatile OrderStatus status;
    private volatile boolean cancelled;
    
    public Order(String userId, String productId, int quantity, double price) {
        this.orderId = "ORD-" + orderIdCounter.getAndIncrement();
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.status = OrderStatus.PENDING;
        this.cancelled = false;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getPrice() {
        return price;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void cancel() {
        this.cancelled = true;
    }
    
    @Override
    public String toString() {
        return String.format("Order[%s, User:%s, Product:%s, Qty:%d, Price:%.2f, Status:%s]",
                orderId, userId, productId, quantity, price, status);
    }
    
    public enum OrderStatus {
        PENDING,
        INVENTORY_CHECKED,
        PAYMENT_PROCESSING,
        PAYMENT_COMPLETED,
        SHIPPING_PREPARED,
        COMPLETED,
        CANCELLED,
        FAILED
    }
}

