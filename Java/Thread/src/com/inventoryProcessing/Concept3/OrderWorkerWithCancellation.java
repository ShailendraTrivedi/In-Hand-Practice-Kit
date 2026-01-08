package com.inventoryProcessing.Concept3;

/**
 * CONCEPT #3: OrderWorker with cancellation support
 * 
 * This worker checks the volatile cancellation flag during processing.
 * Demonstrates how volatile ensures immediate visibility of cancellation.
 */
public class OrderWorkerWithCancellation implements Runnable {
    
    private final Order order;
    private final int workerId;
    private final InventoryService inventoryService;
    
    public OrderWorkerWithCancellation(Order order, int workerId, InventoryService inventoryService) {
        this.order = order;
        this.workerId = workerId;
        this.inventoryService = inventoryService;
    }
    
    /**
     * Process order with cancellation checks.
     * ✅ Uses volatile flag to check cancellation at each step
     */
    @Override
    public void run() {
        try {
            Thread.currentThread().setName("OrderWorker-" + workerId);
            
            System.out.println("[Worker-" + workerId + "] Starting: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // Check cancellation before each step
            // ✅ volatile ensures we see the latest cancellation status
            if (order.isCancelled()) {
                System.out.println("[Worker-" + workerId + "] Order already cancelled: " + order.getOrderId());
                return;
            }
            
            // Step 1: Check and reserve inventory
            System.out.println("[Worker-" + workerId + "] Checking inventory for: " + order.getProductId());
            Thread.sleep(100); // Simulate I/O
            
            // Check cancellation again (order might be cancelled during I/O)
            if (order.isCancelled()) {
                System.out.println("[Worker-" + workerId + "] Order cancelled during inventory check: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.CANCELLED);
                return;
            }
            
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Worker-" + workerId + "] ✗ Insufficient stock");
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            System.out.println("[Worker-" + workerId + "] ✓ Inventory reserved");
            
            // Step 2: Process payment
            System.out.println("[Worker-" + workerId + "] Processing payment: $" + order.getPrice());
            Thread.sleep(150); // Simulate payment gateway call
            
            // Check cancellation again
            if (order.isCancelled()) {
                System.out.println("[Worker-" + workerId + "] Order cancelled during payment: " + order.getOrderId());
                // In real system, we'd refund here
                order.setStatus(Order.OrderStatus.CANCELLED);
                return;
            }
            
            System.out.println("[Worker-" + workerId + "] ✓ Payment processed");
            
            // Step 3: Prepare shipping
            System.out.println("[Worker-" + workerId + "] Preparing shipping");
            Thread.sleep(50);
            
            // Final cancellation check
            if (order.isCancelled()) {
                System.out.println("[Worker-" + workerId + "] Order cancelled during shipping prep: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.CANCELLED);
                return;
            }
            
            // Step 4: Complete order
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            System.out.println("[Worker-" + workerId + "] ✗ Interrupted: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.FAILED);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("[Worker-" + workerId + "] ✗ Error: " + order.getOrderId() + " - " + e.getMessage());
            order.setStatus(Order.OrderStatus.FAILED);
        }
    }
    
    public Order getOrder() {
        return order;
    }
}  