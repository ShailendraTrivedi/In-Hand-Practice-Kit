package com.inventoryProcessing.Concept3;

/**
 * CONCEPT #3 - BEFORE: Order WITHOUT volatile cancellation flag
 * 
 * ⚠️  THIS VERSION HAS VISIBILITY ISSUES ⚠️
 * 
 * Problem:
 * - The 'cancelled' flag is NOT volatile
 * - Worker threads might read stale/cached values
 * - Cancellation might not be detected immediately
 * 
 * Scenario:
 * Thread 1 (Worker): Reads cancelled = false (cached in CPU register)
 * Thread 2 (Cancellation): Writes cancelled = true (in main memory)
 * Thread 1 (Worker): Still sees cancelled = false ❌ (stale read!)
 * 
 * Result: Worker continues processing a cancelled order
 */
public class OrderWithoutVolatile {
    private final String orderId;
    private final String userId;
    private final String productId;
    private final int quantity;
    private final double price;
    private OrderStatus status;
    
    // ⚠️  NOT VOLATILE - Worker threads might not see cancellation immediately
    private boolean cancelled = false;
    
    public OrderWithoutVolatile(String orderId, String userId, String productId, int quantity, double price) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.status = OrderStatus.PENDING;
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public OrderStatus getStatus() { return status; }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    /**
     * Check if order is cancelled.
     * ⚠️  Without volatile, this might return stale value
     */
    public boolean isCancelled() {
        return cancelled; // Might read from CPU cache instead of main memory
    }
    
    /**
     * Cancel the order.
     * ⚠️  Without volatile, this write might not be immediately visible
     */
    public void cancel() {
        this.cancelled = true; // Write might stay in CPU cache
        this.status = OrderStatus.CANCELLED;
    }
    
    @Override
    public String toString() {
        return String.format("Order[%s, Status:%s, Cancelled:%s]", 
                orderId, status, cancelled);
    }
    
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        CANCELLED,
        FAILED
    }
}