package com.inventoryProcessing.Concept12;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CONCEPT #12 - BEFORE: OrderProcessingService WITH Poor Shutdown
 * 
 * ⚠️  THIS VERSION HAS POOR SHUTDOWN HANDLING ⚠️
 * 
 * Problem:
 * - Abrupt shutdown: Tasks may be interrupted mid-execution
 * - No graceful shutdown: Doesn't wait for tasks to complete
 * - Data loss: Orders may be lost or left in inconsistent state
 * - Resource leaks: Threads may not be properly cleaned up
 * - No shutdown hooks: System shutdown not handled gracefully
 * 
 * Issues:
 * - shutdownNow() interrupts all tasks immediately
 * - No timeout handling
 * - Tasks may be in inconsistent state
 * - Resources not properly released
 */
public class OrderProcessingServiceWithPoorShutdown {
    
    private final ExecutorService executorService;
    private volatile boolean running = true;
    
    public OrderProcessingServiceWithPoorShutdown(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
        System.out.println("[Service] Created thread pool: " + poolSize + " threads");
    }
    
    /**
     * Process order.
     */
    public void processOrder(Order order) {
        if (!running) {
            System.out.println("[Service] Not accepting new orders (shutting down)");
            return;
        }
        
        executorService.submit(() -> {
            try {
                System.out.println("[Worker] Processing: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.PROCESSING);
                order.setProcessingStartTime(System.currentTimeMillis());
                
                // Simulate order processing
                Thread.sleep(500); // Long-running task
                
                order.setProcessingEndTime(System.currentTimeMillis());
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Worker] ✓ Completed: " + order.getOrderId());
                
            } catch (InterruptedException e) {
                // ⚠️  Task interrupted during shutdown
                System.out.println("[Worker] ✗ Interrupted: " + order.getOrderId() + 
                                 " (order may be in inconsistent state)");
                order.setStatus(Order.OrderStatus.FAILED);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[Worker] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Poor shutdown: Abrupt termination.
     * ⚠️  PROBLEM: Interrupts all tasks immediately
     */
    public void shutdown() {
        System.out.println("[Service] Shutting down (abrupt)...");
        running = false;
        
        // ⚠️  BAD: shutdownNow() interrupts all tasks immediately
        // Tasks may be in inconsistent state
        executorService.shutdownNow();
        
        System.out.println("[Service] ✗ Shutdown complete (tasks may be incomplete)");
    }
    
    /**
     * Alternative poor shutdown: No waiting.
     * ⚠️  PROBLEM: Doesn't wait for tasks to complete
     */
    public void shutdownNoWait() {
        System.out.println("[Service] Shutting down (no wait)...");
        running = false;
        
        // ⚠️  BAD: shutdown() without awaitTermination()
        // Tasks may still be running when method returns
        executorService.shutdown();
        
        System.out.println("[Service] ✗ Shutdown initiated (tasks may still be running)");
    }
}

