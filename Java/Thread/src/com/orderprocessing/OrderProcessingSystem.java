package com.orderprocessing;

import com.orderprocessing.manager.OrderManager;
import com.orderprocessing.manager.ShutdownManager;
import com.orderprocessing.model.Order;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class that simulates a high-load order processing system.
 * Demonstrates all concurrency concepts in action.
 */
public class OrderProcessingSystem {
    
    private static final int QUEUE_SIZE = 1000;
    private static final int NUM_WORKERS = Runtime.getRuntime().availableProcessors();
    private static final int PAYMENT_THREADS = NUM_WORKERS * 2; // I/O-bound, can use more threads
    private static final int NUM_ORDERS = 5000;
    private static final int ORDER_SUBMISSION_DELAY_MS = 1; // Simulate high load
    
    private final OrderManager orderManager;
    private final ShutdownManager shutdownManager;
    private final AtomicInteger ordersSubmitted = new AtomicInteger(0);
    private final AtomicInteger ordersCompleted = new AtomicInteger(0);
    private final AtomicInteger ordersFailed = new AtomicInteger(0);
    private final AtomicInteger ordersCancelled = new AtomicInteger(0);
    
    public OrderProcessingSystem() {
        this.orderManager = new OrderManager(QUEUE_SIZE, NUM_WORKERS, PAYMENT_THREADS);
        this.shutdownManager = new ShutdownManager();
    }
    
    /**
     * Simulates order submission from multiple concurrent users.
     */
    public void simulateOrderSubmission() {
        System.out.println("=== Order Processing System Started ===");
        System.out.println("Configuration:");
        System.out.println("  - Queue Size: " + QUEUE_SIZE);
        System.out.println("  - Worker Threads: " + NUM_WORKERS);
        System.out.println("  - Payment Threads: " + PAYMENT_THREADS);
        System.out.println("  - Total Orders: " + NUM_ORDERS);
        System.out.println("  - Available CPU Cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println();
        
        List<Thread> producerThreads = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        
        // Create multiple producer threads to simulate concurrent order submissions
        int numProducers = 10;
        int ordersPerProducer = NUM_ORDERS / numProducers;
        
        for (int i = 0; i < numProducers; i++) {
            final int producerId = i;
            Thread producer = new Thread(() -> {
                for (int j = 0; j < ordersPerProducer; j++) {
                    if (shutdownManager.isShutdownRequested()) {
                        break;
                    }
                    
                    try {
                        // Generate random order
                        String userId = "USER-" + ThreadLocalRandom.current().nextInt(1, 1000);
                        String productId = "PROD-00" + (ThreadLocalRandom.current().nextInt(1, 6));
                        int quantity = ThreadLocalRandom.current().nextInt(1, 10);
                        double price = ThreadLocalRandom.current().nextDouble(10.0, 500.0);
                        
                        Order order = new Order(userId, productId, quantity, price);
                        orders.add(order);
                        
                        orderManager.submitOrder(order);
                        ordersSubmitted.incrementAndGet();
                        
                        // Randomly cancel some orders (5% cancellation rate)
                        if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                            orderManager.cancelOrder(order);
                            ordersCancelled.incrementAndGet();
                        }
                        
                        // Simulate network delay
                        Thread.sleep(ORDER_SUBMISSION_DELAY_MS);
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Producer-" + producerId);
            
            producerThreads.add(producer);
            producer.start();
        }
        
        // Wait for all producers to finish
        for (Thread producer : producerThreads) {
            try {
                producer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("\n[System] All orders submitted. Waiting for processing to complete...");
        
        // Monitor processing
        monitorProcessing(orders);
        
        // Graceful shutdown
        try {
            orderManager.shutdown(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[System] Shutdown interrupted");
        }
        
        // Print final statistics
        printStatistics(orders);
    }
    
    /**
     * Monitors order processing and prints statistics.
     */
    private void monitorProcessing(List<Order> orders) {
        long startTime = System.currentTimeMillis();
        long lastPrintTime = startTime;
        
        while (true) {
            try {
                Thread.sleep(1000);
                
                long currentTime = System.currentTimeMillis();
                int completed = countCompleted(orders);
                int failed = countFailed(orders);
                int cancelled = countCancelled(orders);
                int pending = orders.size() - completed - failed - cancelled;
                
                if (currentTime - lastPrintTime >= 2000) {
                    System.out.printf(
                        "[Stats] Queue: %d | Completed: %d | Failed: %d | Cancelled: %d | Pending: %d | Elapsed: %ds%n",
                        orderManager.getOrderQueue().size(),
                        completed,
                        failed,
                        cancelled,
                        pending,
                        (currentTime - startTime) / 1000
                    );
                    lastPrintTime = currentTime;
                }
                
                // Check if processing is complete
                if (orderManager.getOrderQueue().isEmpty() && pending == 0) {
                    System.out.println("\n[System] All orders processed!");
                    break;
                }
                
                // Timeout after 2 minutes
                if (currentTime - startTime > 120000) {
                    System.out.println("\n[System] Processing timeout reached");
                    break;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private int countCompleted(List<Order> orders) {
        return (int) orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
            .count();
    }
    
    private int countFailed(List<Order> orders) {
        return (int) orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.FAILED)
            .count();
    }
    
    private int countCancelled(List<Order> orders) {
        return (int) orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .count();
    }
    
    /**
     * Prints final statistics.
     */
    private void printStatistics(List<Order> orders) {
        System.out.println("\n=== Final Statistics ===");
        System.out.println("Total Orders Submitted: " + ordersSubmitted.get());
        System.out.println("Orders Completed: " + countCompleted(orders));
        System.out.println("Orders Failed: " + countFailed(orders));
        System.out.println("Orders Cancelled: " + countCancelled(orders));
        System.out.println("\nInventory Status:");
        System.out.println("  PROD-001: " + orderManager.getInventoryService().getStock("PROD-001"));
        System.out.println("  PROD-002: " + orderManager.getInventoryService().getStock("PROD-002"));
        System.out.println("  PROD-003: " + orderManager.getInventoryService().getStock("PROD-003"));
        System.out.println("  PROD-004: " + orderManager.getInventoryService().getStock("PROD-004"));
        System.out.println("  PROD-005: " + orderManager.getInventoryService().getStock("PROD-005"));
    }
    
    public static void main(String[] args) {
        OrderProcessingSystem system = new OrderProcessingSystem();
        system.simulateOrderSubmission();
    }
}

