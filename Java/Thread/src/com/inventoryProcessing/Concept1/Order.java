package com.inventoryProcessing.Concept1;

/**
 * Simple Order model for Concept #1 demonstration.
 * This will evolve as we add more concepts.
 */
public class Order {
    private final String orderId;
    private final String userId;
    private final String productId;
    private final int quantity;
    private final double price;
    private OrderStatus status;
    
    public Order(String orderId, String userId, String productId, int quantity, double price) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.status = OrderStatus.PENDING;
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public OrderStatus getStatus() { return status; }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return String.format("Order[%s, User:%s, Product:%s, Qty:%d, Price:%.2f, Status:%s]",
                orderId, userId, productId, quantity, price, status);
    }
    
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}