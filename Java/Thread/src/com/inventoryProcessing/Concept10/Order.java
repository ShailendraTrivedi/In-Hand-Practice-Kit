package com.inventoryProcessing.Concept10;

/**
 * Order model for Concept #10.
 * Self-contained within Concept10 folder.
 */
public class Order {
    private final String orderId;
    private final String userId;
    private final String productId;
    private final int quantity;
    private final double price;
    private OrderStatus status;
    private volatile boolean cancelled = false;
    private OrderPriority priority;
    
    public Order(String orderId, String userId, String productId, int quantity, double price) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.status = OrderStatus.PENDING;
        this.priority = OrderPriority.NORMAL;
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public OrderStatus getStatus() { return status; }
    public OrderPriority getPriority() { return priority; }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public void setPriority(OrderPriority priority) {
        this.priority = priority;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void cancel() {
        this.cancelled = true;
        this.status = OrderStatus.CANCELLED;
    }
    
    @Override
    public String toString() {
        return String.format("Order[%s, User:%s, Product:%s, Qty:%d, Price:%.2f, Status:%s, Priority:%s]",
                orderId, userId, productId, quantity, price, status, priority);
    }
    
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        CANCELLED,
        FAILED
    }
    
    public enum OrderPriority {
        LOW,
        NORMAL,
        HIGH,
        VIP
    }
}