package com.inventoryProcessing.Concept5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CONCEPT #5 - AFTER: OrderProcessingService WITH ExecutorService
 * 
 * ✅ THIS VERSION USES THREAD POOL (EFFICIENT) ✅
 * 
 * Definition:
 * - ExecutorService: High-level API for managing thread execution
 * - Thread Pool: Collection of reusable worker threads
 * - Task Queue: Holds tasks waiting to be executed
 * 
 * Why needed in e-commerce:
 * - Reuses threads instead of creating new ones (faster)
 * - Controls maximum concurrency (prevents resource exhaustion)
 * - Better resource management (limited thread count)
 * - Easier lifecycle management (shutdown, await termination)
 * - Improved performance (thread creation is expensive)
 * 
 * Safety Measure:
 * - ExecutorService: Manages thread lifecycle automatically
 * - Thread pool: Reuses threads, reducing overhead
 * - Controlled concurrency: Limits maximum threads
 * - Graceful shutdown: Properly terminates all tasks
 * 
 * Interview Tip:
 * - FixedThreadPool: Fixed number of threads
 * - CachedThreadPool: Creates threads as needed (unbounded)
 * - ScheduledThreadPool: For scheduled/repeated tasks
 * - Custom ThreadPoolExecutor: Full control over pool parameters
 */
public class OrderProcessingService {
    
    private final InventoryService inventoryService;
    private final ExecutorService executorService;
    private final AtomicInteger tasksSubmitted = new AtomicInteger(0);
    private final AtomicInteger tasksCompleted = new AtomicInteger(0);
    
    /**
     * Create order processing service with thread pool.
     * 
     * @param inventoryService Inventory service
     * @param poolSize Number of threads in the pool
     */
    public OrderProcessingService(InventoryService inventoryService, int poolSize) {
        this.inventoryService = inventoryService;
        
        // ✅ Create thread pool with fixed number of threads
        // Threads are reused for multiple tasks
        this.executorService = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setDaemon(false); // Non-daemon threads
            t.setName("OrderProcessor-" + t.getId());
            return t;
        });
        
        System.out.println("[Service] Created thread pool with " + poolSize + " threads");
    }
    
    /**
     * Process order by submitting to thread pool.
     * ✅ EFFICIENT: Reuses existing threads from pool
     * 
     * How it works:
     * 1. Submit task to ExecutorService
     * 2. ExecutorService assigns task to available thread from pool
     * 3. If no thread available, task waits in queue
     * 4. Thread executes task and returns to pool for reuse
     */
    public void processOrder(Order order) {
        tasksSubmitted.incrementAndGet();
        
        // ✅ Submit task to thread pool (reuses threads)
        executorService.submit(() -> {
            try {
                processOrderTask(order);
                tasksCompleted.incrementAndGet();
            } catch (Exception e) {
                System.err.println("[Service] Error processing order: " + order.getOrderId() + " - " + e.getMessage());
            }
        });
    }
    
    /**
     * Process a single order.
     */
    private void processOrderTask(Order order) {
        try {
            System.out.println("[Thread-" + Thread.currentThread().getName() + "] Processing: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // Check inventory
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Thread-" + Thread.currentThread().getName() + "] ✗ Insufficient stock");
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            // Simulate payment processing
            Thread.sleep(100);
            
            // Simulate shipping preparation
            Thread.sleep(50);
            
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Thread-" + Thread.currentThread().getName() + "] ✓ Completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            System.out.println("[Thread-" + Thread.currentThread().getName() + "] ✗ Interrupted");
            order.setStatus(Order.OrderStatus.FAILED);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Wait for all submitted tasks to complete.
     * ✅ EFFICIENT: ExecutorService handles this automatically
     */
    public void waitForCompletion() throws InterruptedException {
        System.out.println("[Service] Waiting for " + tasksSubmitted.get() + " tasks to complete...");
        
        // ✅ Shutdown executor (stops accepting new tasks)
        executorService.shutdown();
        
        // ✅ Wait for all tasks to complete (with timeout)
        boolean terminated = executorService.awaitTermination(30, TimeUnit.SECONDS);
        
        if (!terminated) {
            System.out.println("[Service] ⚠️  Timeout: Some tasks may still be running");
            // Force shutdown if needed
            executorService.shutdownNow();
        }
        
        System.out.println("[Service] ✓ All tasks completed. Processed: " + tasksCompleted.get() + "/" + tasksSubmitted.get());
    }
    
    /**
     * Shutdown the service gracefully.
     * ✅ EFFICIENT: ExecutorService handles shutdown properly
     */
    public void shutdown() {
        System.out.println("[Service] Shutting down...");
        executorService.shutdown();
        
        try {
            // Wait for running tasks to finish
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("[Service] Force shutting down...");
                executorService.shutdownNow();
                
                // Wait again
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("[Service] ✗ ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[Service] ✓ Shutdown complete");
    }
    
    /**
     * Get statistics.
     */
    public void printStatistics() {
        System.out.println("\n=== Service Statistics ===");
        System.out.println("Tasks submitted: " + tasksSubmitted.get());
        System.out.println("Tasks completed: " + tasksCompleted.get());
        System.out.println("Pool size: " + ((java.util.concurrent.ThreadPoolExecutor) executorService).getPoolSize());
        System.out.println("Active threads: " + ((java.util.concurrent.ThreadPoolExecutor) executorService).getActiveCount());
        System.out.println("Queue size: " + ((java.util.concurrent.ThreadPoolExecutor) executorService).getQueue().size());
    }
}