package com.inventoryProcessing.Concept12;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CONCEPT #12 - AFTER: OrderProcessingService WITH Proper Shutdown
 * 
 * ✅ THIS VERSION HAS PROPER SHUTDOWN HANDLING ✅
 * 
 * Definition:
 * - Graceful Shutdown: Allow in-flight tasks to complete before terminating
 * - Shutdown sequence: Stop accepting new tasks, wait for completion, force if needed
 * - Resource cleanup: Properly release all resources
 * - Shutdown hooks: Handle system shutdown gracefully
 * 
 * Why needed in e-commerce:
 * - Data integrity: Ensure all orders are processed or safely queued
 * - No data loss: Prevent orders from being lost during shutdown
 * - Consistent state: Orders should not be left in inconsistent state
 * - Resource cleanup: Release database connections, file handles, etc.
 * - Professional operation: Proper shutdown is critical for production systems
 * 
 * Safety Measure:
 * - Graceful shutdown: Wait for tasks to complete with timeout
 * - Two-phase shutdown: shutdown() then awaitTermination()
 * - Force shutdown: shutdownNow() only if timeout exceeded
 * - Resource cleanup: Close connections, release locks, etc.
 * - Shutdown hooks: Register JVM shutdown hooks for system shutdown
 * 
 * Interview Tip:
 * - Always shutdown ExecutorService properly
 * - Use shutdown() then awaitTermination() with timeout
 * - Handle InterruptedException properly
 * - Clean up resources in finally blocks
 * - Register shutdown hooks for system shutdown
 */
public class OrderProcessingService {
    
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final long shutdownTimeoutSeconds;
    
    /**
     * Create service with proper shutdown configuration.
     * 
     * @param poolSize Thread pool size
     * @param shutdownTimeoutSeconds Timeout for graceful shutdown
     */
    public OrderProcessingService(int poolSize, long shutdownTimeoutSeconds) {
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
        
        this.executorService = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "OrderWorker-" + System.nanoTime());
            t.setDaemon(false); // Non-daemon threads
            return t;
        });
        
        // ✅ Register shutdown hook for JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[ShutdownHook] JVM shutting down, initiating graceful shutdown...");
            try {
                gracefulShutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ShutdownHook] Shutdown interrupted");
            }
        }, "ShutdownHook"));
        
        System.out.println("[Service] Created thread pool: " + poolSize + " threads");
        System.out.println("[Service] Shutdown timeout: " + shutdownTimeoutSeconds + " seconds");
    }
    
    /**
     * Process order.
     * ✅ Checks shutdown status before accepting new tasks
     */
    public void processOrder(Order order) {
        if (shutdown.get()) {
            System.out.println("[Service] Not accepting new orders (shutdown in progress)");
            order.setStatus(Order.OrderStatus.FAILED);
            return;
        }
        
        if (!running.get()) {
            System.out.println("[Service] Not accepting new orders (service stopped)");
            order.setStatus(Order.OrderStatus.FAILED);
            return;
        }
        
        executorService.submit(() -> {
            try {
                System.out.println("[Worker] Processing: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.PROCESSING);
                order.setProcessingStartTime(System.currentTimeMillis());
                
                // Check for shutdown during processing
                if (shutdown.get()) {
                    System.out.println("[Worker] Shutdown requested, completing current task: " + order.getOrderId());
                }
                
                // Simulate order processing
                Thread.sleep(500); // Long-running task
                
                // Check interrupt status
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Task interrupted during shutdown");
                }
                
                order.setProcessingEndTime(System.currentTimeMillis());
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Worker] ✓ Completed: " + order.getOrderId());
                
            } catch (InterruptedException e) {
                // ✅ Proper interrupt handling
                System.out.println("[Worker] ✗ Interrupted: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.CANCELLED);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[Worker] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Graceful shutdown: Two-phase approach.
     * ✅ PROPER: Stop accepting new tasks, wait for completion, force if needed
     * 
     * Phase 1: Stop accepting new tasks (shutdown())
     * Phase 2: Wait for running tasks to complete (awaitTermination())
     * Phase 3: Force shutdown if timeout exceeded (shutdownNow())
     */
    public void gracefulShutdown() throws InterruptedException {
        if (shutdown.getAndSet(true)) {
            System.out.println("[Service] Shutdown already in progress");
            return;
        }
        
        System.out.println("[Service] Initiating graceful shutdown...");
        running.set(false);
        
        // ✅ Phase 1: Stop accepting new tasks
        System.out.println("[Service] Phase 1: Stopping acceptance of new tasks...");
        executorService.shutdown();
        
        try {
            // ✅ Phase 2: Wait for running tasks to complete (with timeout)
            System.out.println("[Service] Phase 2: Waiting for " + shutdownTimeoutSeconds + 
                             " seconds for tasks to complete...");
            
            boolean terminated = executorService.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS);
            
            if (terminated) {
                System.out.println("[Service] ✓ All tasks completed gracefully");
            } else {
                // ✅ Phase 3: Force shutdown if timeout exceeded
                System.out.println("[Service] ⚠️  Timeout reached, forcing shutdown...");
                
                List<Runnable> pendingTasks = executorService.shutdownNow();
                System.out.println("[Service] Cancelled " + pendingTasks.size() + " pending tasks");
                
                // Wait a bit more for tasks to respond to interrupt
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("[Service] ✗ Some tasks did not terminate");
                } else {
                    System.out.println("[Service] ✓ All tasks terminated");
                }
            }
            
        } catch (InterruptedException e) {
            // ✅ Handle interrupt during shutdown
            System.err.println("[Service] ✗ Shutdown interrupted, forcing immediate shutdown");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[Service] ✓ Shutdown complete");
    }
    
    /**
     * Quick shutdown: For testing or emergency situations.
     * ⚠️  Use with caution: May interrupt in-flight tasks
     */
    public void quickShutdown() {
        System.out.println("[Service] Quick shutdown requested...");
        shutdown.set(true);
        running.set(false);
        
        executorService.shutdownNow();
        
        System.out.println("[Service] ✓ Quick shutdown complete");
    }
    
    /**
     * Check if service is running.
     */
    public boolean isRunning() {
        return running.get() && !shutdown.get();
    }
    
    /**
     * Check if shutdown is in progress.
     */
    public boolean isShutdown() {
        return shutdown.get();
    }
    
    /**
     * Get executor statistics.
     */
    public void printStatistics() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            System.out.println("\n=== Executor Statistics ===");
            System.out.println("Pool size: " + tpe.getPoolSize());
            System.out.println("Active threads: " + tpe.getActiveCount());
            System.out.println("Queue size: " + tpe.getQueue().size());
            System.out.println("Completed tasks: " + tpe.getCompletedTaskCount());
            System.out.println("Running: " + isRunning());
            System.out.println("Shutdown: " + isShutdown());
        }
    }
}

