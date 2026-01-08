package com.inventoryProcessing.Concept3;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for cancelling orders.
 * Demonstrates how cancellation works with volatile flags.
 */
public class OrderCancellationService {
    
    // Store active orders (using thread-safe collection - we'll cover this in Concept #8)
    private final ConcurrentMap<String, Order> activeOrders = new ConcurrentHashMap<>();
    
    /**
     * Register an order for potential cancellation.
     */
    public void registerOrder(Order order) {
        activeOrders.put(order.getOrderId(), order);
    }
    
    /**
     * Cancel an order.
     * ✅ The volatile flag ensures this cancellation is immediately visible
     * to all worker threads processing this order.
     * 
     * @param orderId The order to cancel
     * @return true if order was found and cancelled, false otherwise
     */
    public boolean cancelOrder(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order != null) {
            System.out.println("[CancellationService] Cancelling order: " + orderId);
            order.cancel(); // ✅ This write to volatile is immediately visible
            return true;
        }
        return false;
    }
    
    /**
     * Cancel an order by Order object.
     */
    public void cancelOrder(Order order) {
        if (order != null) {
            System.out.println("[CancellationService] Cancelling order: " + order.getOrderId());
            order.cancel(); // ✅ Volatile write - immediately visible
        }
    }
    
    /**
     * Remove order from active tracking (when completed or failed).
     */
    public void removeOrder(String orderId) {
        activeOrders.remove(orderId);
    }
}