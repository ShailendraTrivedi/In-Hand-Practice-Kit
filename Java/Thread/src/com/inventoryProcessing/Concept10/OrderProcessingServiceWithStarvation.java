package com.inventoryProcessing.Concept10;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CONCEPT #10 - BEFORE: OrderProcessingService WITH Thread Starvation
 * 
 * ⚠️  THIS VERSION CAUSES THREAD STARVATION ⚠️
 * 
 * Problem:
 * - Single thread pool for all tasks (CPU-bound and I/O-bound)
 * - I/O-bound tasks block threads (waiting for network/database)
 * - CPU-bound tasks wait in queue (starvation)
 * - Low-priority tasks never get processed (starvation)
 * 
 * Starvation Scenarios:
 * 1. I/O tasks block threads → CPU tasks wait indefinitely
 * 2. High-priority tasks always selected → Low-priority tasks starve
 * 3. Long-running tasks occupy threads → Short tasks wait
 * 4. Single pool size doesn't match task characteristics
 */
public class OrderProcessingServiceWithStarvation {
    
    private final ExecutorService executorService;
    private final int poolSize;
    
    /**
     * Create service with single thread pool.
     * ⚠️  PROBLEM: One size doesn't fit all task types
     */
    public OrderProcessingServiceWithStarvation(int poolSize) {
        this.poolSize = poolSize;
        // ⚠️  Single pool for all tasks (CPU-bound and I/O-bound)
        this.executorService = Executors.newFixedThreadPool(poolSize);
        System.out.println("[Service] Created single thread pool with " + poolSize + " threads");
        System.out.println("⚠️  Warning: All tasks (CPU and I/O) share same pool");
    }
    
    /**
     * Process CPU-bound task (inventory calculation).
     * ⚠️  PROBLEM: If I/O tasks are running, CPU tasks wait
     */
    public void processCpuBoundTask(Order order) {
        executorService.submit(() -> {
            try {
                System.out.println("[CPU-Task] Processing: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.PROCESSING);
                
                // CPU-intensive calculation
                calculateInventory(order);
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[CPU-Task] ✓ Completed: " + order.getOrderId());
                
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[CPU-Task] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Process I/O-bound task (payment processing).
     * ⚠️  PROBLEM: Blocks thread while waiting for I/O
     * This prevents CPU tasks from running
     */
    public void processIoBoundTask(Order order) {
        executorService.submit(() -> {
            try {
                System.out.println("[I/O-Task] Processing: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.PROCESSING);
                
                // I/O operation (blocks thread)
                processPayment(order);
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[I/O-Task] ✓ Completed: " + order.getOrderId());
                
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[I/O-Task] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Process order (mixed CPU and I/O).
     * ⚠️  PROBLEM: Long-running task blocks thread
     */
    public void processOrder(Order order) {
        executorService.submit(() -> {
            try {
                System.out.println("[Mixed-Task] Processing: " + order.getOrderId() + 
                                 " (Priority: " + order.getPriority() + ")");
                order.setStatus(Order.OrderStatus.PROCESSING);
                
                // CPU-bound work
                calculateInventory(order);
                
                // I/O-bound work
                processPayment(order);
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Mixed-Task] ✓ Completed: " + order.getOrderId());
                
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[Mixed-Task] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * CPU-intensive calculation.
     */
    private void calculateInventory(Order order) {
        // Simulate CPU-intensive work
        long result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += i * order.getQuantity();
        }
    }
    
    /**
     * I/O operation (simulates network/database call).
     */
    private void processPayment(Order order) throws InterruptedException {
        // Simulate I/O wait (network call, database query)
        Thread.sleep(200); // Blocks thread during I/O
    }
    
    /**
     * Shutdown service.
     */
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }
}