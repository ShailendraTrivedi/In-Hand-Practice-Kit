package com.inventoryProcessing.Concept7;

/**
 * CONCEPT #7 - BEFORE: OrderWorker IGNORING Interrupts
 * 
 * ⚠️  THIS VERSION IGNORES INTERRUPTS (BAD PRACTICE) ⚠️
 * 
 * Problem:
 * - Ignores InterruptedException
 * - Thread cannot be stopped gracefully
 * - Continues processing even when interrupted
 * - Wastes resources and prevents shutdown
 * 
 * Issues:
 * - Swallows InterruptedException
 * - Doesn't check Thread.interrupted()
 * - Cannot be cancelled during long operations
 * - Blocks shutdown process
 */
public class OrderWorkerIgnoringInterrupts implements Runnable {
    
    private final Order order;
    private final int workerId;
    private final InventoryService inventoryService;
    
    public OrderWorkerIgnoringInterrupts(Order order, int workerId, InventoryService inventoryService) {
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
            
            // Step 1: Check inventory
            System.out.println("[Worker-" + workerId + "] Checking inventory...");
            Thread.sleep(100); // ⚠️  If interrupted, exception is caught but ignored
            
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            // Step 2: Process payment (long operation)
            System.out.println("[Worker-" + workerId + "] Processing payment...");
            Thread.sleep(500); // ⚠️  If interrupted, exception is caught but ignored
            
            // Step 3: Prepare shipping
            System.out.println("[Worker-" + workerId + "] Preparing shipping...");
            Thread.sleep(200); // ⚠️  If interrupted, exception is caught but ignored
            
            // Step 4: Complete order
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            // ⚠️  BAD: Swallows the interrupt
            // Thread interrupt flag is cleared, but we ignore it
            // Thread continues as if nothing happened
            System.out.println("[Worker-" + workerId + "] Interrupted (but ignoring it)");
            // ❌ Missing: Thread.currentThread().interrupt();
            // ❌ Missing: return or proper cleanup
            order.setStatus(Order.OrderStatus.FAILED);
        }
    }
}

