package com.inventoryProcessing.Concept3;

/**
 * CONCEPT #3 - AFTER: Order WITH volatile cancellation flag
 * 
 * ✅ THIS VERSION HAS PROPER VISIBILITY ✅
 * 
 * Definition:
 * - volatile: Ensures that reads/writes to a variable are visible to all threads
 * - Visibility: Changes are immediately written to main memory and read from main memory
 * - Happens-before: Write to volatile happens-before any subsequent read
 * 
 * Why needed in e-commerce:
 * - Order cancellation must be immediately visible to all worker threads
 * - Prevents wasted processing on cancelled orders
 * - Ensures workers can stop processing as soon as order is cancelled
 * 
 * Safety Measure:
 * - volatile keyword: Guarantees visibility across threads
 * - All reads/writes go directly to main memory (no CPU cache)
 * - No compiler reordering around volatile operations
 * 
 * Interview Tip:
 * - volatile provides visibility, NOT atomicity
 * - For compound operations (like count++), you still need synchronized
 * - volatile is perfect for flags and single-value updates
 */
public class Order {
    private final String orderId;
    private final String userId;
    private final String productId;
    private final int quantity;
    private final double price;
    private OrderStatus status;
    
    // ✅ VOLATILE: Ensures immediate visibility to all threads
    // When one thread sets this to true, ALL threads see it immediately
    private volatile boolean cancelled = false;
    
    public Order(String orderId, String userId, String productId, int quantity, double price) {
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
     * ✅ With volatile, this always reads the latest value from main memory
     * 
     * How volatile works:
     * 1. Read always goes to main memory (not CPU cache)
     * 2. Ensures we see the most recent write
     * 3. No compiler optimizations that might cache the value
     */
    public boolean isCancelled() {
        return cancelled; // Always reads from main memory
    }
    
    /**
     * Cancel the order.
     * ✅ With volatile, this write is immediately visible to all threads
     * 
     * How volatile works:
     * 1. Write immediately goes to main memory (not just CPU cache)
     * 2. All other threads see this change immediately
     * 3. No compiler reordering around this operation
     */
    public void cancel() {
        this.cancelled = true; // Immediately visible to all threads
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