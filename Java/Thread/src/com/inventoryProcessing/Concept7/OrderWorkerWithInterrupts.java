package com.inventoryProcessing.Concept7;

/**
 * CONCEPT #7 - AFTER: OrderWorker WITH Proper Interrupt Handling
 * 
 * ✅ THIS VERSION HANDLES INTERRUPTS CORRECTLY ✅
 * 
 * Definition:
 * - Interruption: Cooperative mechanism to signal thread to stop
 * - InterruptedException: Thrown when thread is interrupted during blocking operation
 * - Thread.interrupted(): Checks and clears interrupt flag
 * - Thread.currentThread().interrupt(): Restores interrupt flag
 * 
 * Why needed in e-commerce:
 * - Graceful shutdown: Stop processing orders during system shutdown
 * - Order cancellation: Stop processing when order is cancelled
 * - Resource cleanup: Release resources when interrupted
 * - Responsive system: Respond to cancellation requests quickly
 * 
 * Safety Measure:
 * - Proper interrupt handling: Check interrupt status and respond
 * - Restore interrupt flag: Call Thread.currentThread().interrupt()
 * - Cleanup resources: Release locks, close connections, etc.
 * - Exit gracefully: Return or throw InterruptedException
 * 
 * Interview Tip:
 * - Always restore interrupt flag: Thread.currentThread().interrupt()
 * - Check interrupt status in loops: while (!Thread.interrupted())
 * - Don't swallow InterruptedException
 * - Clean up resources before exiting
 * - Use interrupt for graceful cancellation
 */
public class OrderWorkerWithInterrupts implements Runnable {
    
    private final Order order;
    private final int workerId;
    private final InventoryService inventoryService;
    
    public OrderWorkerWithInterrupts(Order order, int workerId, InventoryService inventoryService) {
        this.order = order;
        this.workerId = workerId;
        this.inventoryService = inventoryService;
    }
    
    @Override
    public void run() {
        try {
            Thread.currentThread().setName("Worker-" + workerId);
            System.out.println("[Worker-" + workerId + "] Starting: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // ✅ Check interrupt status before starting work
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("[Worker-" + workerId + "] Already interrupted, exiting");
                order.setStatus(Order.OrderStatus.CANCELLED);
                return;
            }
            
            // Step 1: Check inventory
            System.out.println("[Worker-" + workerId + "] Checking inventory...");
            Thread.sleep(100); // ✅ Throws InterruptedException if interrupted
            
            // ✅ Check interrupt after blocking operation
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("[Worker-" + workerId + "] Interrupted during inventory check");
                order.setStatus(Order.OrderStatus.CANCELLED);
                return;
            }
            
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            // Step 2: Process payment (long operation)
            System.out.println("[Worker-" + workerId + "] Processing payment...");
            
            // ✅ Check interrupt in long-running operations
            for (int i = 0; i < 5; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("[Worker-" + workerId + "] Interrupted during payment processing");
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    return;
                }
                Thread.sleep(100); // Simulate payment steps
            }
            
            // Step 3: Prepare shipping
            System.out.println("[Worker-" + workerId + "] Preparing shipping...");
            Thread.sleep(200); // ✅ Throws InterruptedException if interrupted
            
            // ✅ Final interrupt check
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("[Worker-" + workerId + "] Interrupted during shipping prep");
                order.setStatus(Order.OrderStatus.CANCELLED);
                return;
            }
            
            // Step 4: Complete order
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            // ✅ GOOD: Handle interrupt properly
            System.out.println("[Worker-" + workerId + "] ✗ Interrupted: " + order.getOrderId());
            
            // ✅ CRITICAL: Restore interrupt flag
            // This allows calling code to know thread was interrupted
            Thread.currentThread().interrupt();
            
            // ✅ Cleanup: Mark order as cancelled
            order.setStatus(Order.OrderStatus.CANCELLED);
            
            // ✅ Exit gracefully (don't continue processing)
            return;
        }
    }
}

