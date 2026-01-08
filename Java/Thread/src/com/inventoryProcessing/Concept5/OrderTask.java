package com.inventoryProcessing.Concept5;

/**
 * CONCEPT #5: OrderTask
 * 
 * A Runnable task that processes a single order.
 * This task is submitted to ExecutorService.
 */
public class OrderTask implements Runnable {
    
    private final Order order;
    private final InventoryService inventoryService;
    
    public OrderTask(Order order, InventoryService inventoryService) {
        this.order = order;
        this.inventoryService = inventoryService;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("[Task-" + Thread.currentThread().getName() + "] Processing: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // Check inventory
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Task-" + Thread.currentThread().getName() + "] ✗ Insufficient stock");
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            // Simulate payment processing
            Thread.sleep(100);
            
            // Simulate shipping preparation
            Thread.sleep(50);
            
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Task-" + Thread.currentThread().getName() + "] ✓ Completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            System.out.println("[Task-" + Thread.currentThread().getName() + "] ✗ Interrupted");
            order.setStatus(Order.OrderStatus.FAILED);
            Thread.currentThread().interrupt();
        }
    }
    
    public Order getOrder() {
        return order;
    }
}