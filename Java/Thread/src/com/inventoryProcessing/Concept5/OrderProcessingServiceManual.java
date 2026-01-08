package com.inventoryProcessing.Concept5;

import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT #5 - BEFORE: Manual Thread Management
 * 
 * ⚠️  THIS VERSION CREATES THREADS MANUALLY (INEFFICIENT) ⚠️
 * 
 * Problems:
 * - Creates a new thread for each order (expensive)
 * - No control over maximum number of threads
 * - Difficult to manage thread lifecycle
 * - No easy way to wait for all tasks to complete
 * - Resource waste: threads created/destroyed frequently
 * 
 * Issues:
 * - Thread creation overhead (~1ms per thread)
 * - Memory overhead (each thread has ~1MB stack)
 * - No thread reuse
 * - Difficult to shutdown gracefully
 */
public class OrderProcessingServiceManual {
    
    private final InventoryService inventoryService;
    private final List<Thread> activeThreads = new ArrayList<>();
    private volatile boolean shutdown = false;
    
    public OrderProcessingServiceManual(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * Process order by creating a new thread.
     * ⚠️  INEFFICIENT: Creates new thread for each order
     */
    public void processOrder(Order order) {
        if (shutdown) {
            System.out.println("[Service] Shutdown, rejecting order: " + order.getOrderId());
            return;
        }
        
        // ⚠️  Creates a new thread for each order
        Thread thread = new Thread(() -> {
            processOrderTask(order);
        }, "OrderThread-" + order.getOrderId());
        
        thread.start();
        
        synchronized (activeThreads) {
            activeThreads.add(thread);
        }
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
        } finally {
            // Remove from active threads
            synchronized (activeThreads) {
                activeThreads.remove(Thread.currentThread());
            }
        }
    }
    
    /**
     * Wait for all orders to complete.
     * ⚠️  INEFFICIENT: Must manually track and join all threads
     */
    public void waitForCompletion() throws InterruptedException {
        List<Thread> threadsToWait;
        synchronized (activeThreads) {
            threadsToWait = new ArrayList<>(activeThreads);
        }
        
        for (Thread thread : threadsToWait) {
            thread.join();
        }
    }
    
    /**
     * Shutdown the service.
     * ⚠️  DIFFICULT: Must manually interrupt all threads
     */
    public void shutdown() {
        shutdown = true;
        synchronized (activeThreads) {
            for (Thread thread : activeThreads) {
                thread.interrupt();
            }
        }
    }
    
    public int getActiveThreadCount() {
        synchronized (activeThreads) {
            return activeThreads.size();
        }
    }
}