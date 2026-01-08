package com.inventoryProcessing.Concept7;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CONCEPT #7: OrderProcessingService with Graceful Shutdown
 * 
 * Demonstrates proper interrupt handling and graceful shutdown.
 */
public class OrderProcessingService {
    
    private final InventoryService inventoryService;
    private final ExecutorService executorService;
    private final List<Thread> workerThreads;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    
    public OrderProcessingService(InventoryService inventoryService, int poolSize) {
        this.inventoryService = inventoryService;
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.workerThreads = new ArrayList<>();
    }
    
    /**
     * Process order using ExecutorService.
     */
    public void processOrder(Order order) {
        if (shutdown.get()) {
            System.out.println("[Service] Shutdown, rejecting order: " + order.getOrderId());
            return;
        }
        
        executorService.submit(new OrderWorkerWithInterrupts(order, 
            System.identityHashCode(Thread.currentThread()), inventoryService));
    }
    
    /**
     * Process order with manual thread management (for interrupt demo).
     */
    public Thread processOrderWithThread(Order order, int workerId) {
        if (shutdown.get()) {
            System.out.println("[Service] Shutdown, rejecting order: " + order.getOrderId());
            return null;
        }
        
        OrderWorkerWithInterrupts worker = new OrderWorkerWithInterrupts(order, workerId, inventoryService);
        Thread thread = new Thread(worker, "Worker-" + workerId);
        thread.start();
        
        synchronized (workerThreads) {
            workerThreads.add(thread);
        }
        
        return thread;
    }
    
    /**
     * Graceful shutdown using ExecutorService.
     * ✅ Properly handles interrupts
     */
    public void shutdown() throws InterruptedException {
        System.out.println("[Service] Initiating graceful shutdown...");
        shutdown.set(true);
        
        // Stop accepting new tasks
        executorService.shutdown();
        
        try {
            // Wait for running tasks to complete (with timeout)
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("[Service] Timeout reached, forcing shutdown...");
                
                // ✅ Force shutdown: interrupts all running tasks
                List<Runnable> pendingTasks = executorService.shutdownNow();
                System.out.println("[Service] Cancelled " + pendingTasks.size() + " pending tasks");
                
                // Wait again for tasks to respond to interrupt
                if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                    System.err.println("[Service] ✗ Some tasks did not terminate");
                }
            }
        } catch (InterruptedException e) {
            // ✅ Restore interrupt flag
            Thread.currentThread().interrupt();
            
            // Force shutdown
            executorService.shutdownNow();
        }
        
        System.out.println("[Service] ✓ Shutdown complete");
    }
    
    /**
     * Shutdown manual threads (for interrupt demo).
     * ✅ Properly interrupts threads
     */
    public void shutdownManualThreads() {
        System.out.println("[Service] Interrupting " + workerThreads.size() + " worker threads...");
        
        synchronized (workerThreads) {
            for (Thread thread : workerThreads) {
                if (thread.isAlive()) {
                    System.out.println("[Service] Interrupting: " + thread.getName());
                    // ✅ Interrupt the thread (sets interrupt flag)
                    thread.interrupt();
                }
            }
        }
    }
    
    /**
     * Wait for all manual threads to complete.
     */
    public void waitForThreads(long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        
        synchronized (workerThreads) {
            for (Thread thread : new ArrayList<>(workerThreads)) {
                if (thread.isAlive()) {
                    long remaining = timeoutMs - (System.currentTimeMillis() - startTime);
                    if (remaining > 0) {
                        thread.join(remaining);
                    }
                }
            }
        }
    }
}

