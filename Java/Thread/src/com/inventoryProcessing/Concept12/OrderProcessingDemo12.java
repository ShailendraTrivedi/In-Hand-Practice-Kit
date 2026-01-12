package com.inventoryProcessing.Concept12;

import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT #12 DEMONSTRATION: Proper Shutdown Handling
 * 
 * This demo shows:
 * 1. BEFORE: Poor shutdown (abrupt termination, data loss)
 * 2. AFTER: Proper shutdown (graceful, ensures completion)
 * 
 * Interview Tip:
 * - Always shutdown ExecutorService properly
 * - Use shutdown() then awaitTermination() with timeout
 * - Handle InterruptedException during shutdown
 * - Clean up resources in finally blocks
 * - Register shutdown hooks for system shutdown
 */
public class OrderProcessingDemo12 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #12: Proper Shutdown Handling ===\n");
        
        // ============================================
        // PART 1: Poor shutdown (BEFORE)
        // ============================================
        System.out.println("--- PART 1: Poor Shutdown (BEFORE) ---\n");
        demonstratePoorShutdown();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Proper shutdown (AFTER)
        // ============================================
        System.out.println("\n\n--- PART 2: Proper Shutdown (AFTER) ---\n");
        demonstrateProperShutdown();
        
        // ============================================
        // PART 3: Shutdown hook demonstration
        // ============================================
        System.out.println("\n\n--- PART 3: Shutdown Hook (System Shutdown) ---\n");
        demonstrateShutdownHook();
    }
    
    /**
     * Demonstrates poor shutdown handling.
     * ⚠️  Tasks may be interrupted, data may be lost
     */
    private static void demonstratePoorShutdown() throws InterruptedException {
        System.out.println("Demonstrating poor shutdown (abrupt termination)...");
        System.out.println("⚠️  Tasks may be interrupted mid-execution\n");
        
        OrderProcessingServiceWithPoorShutdown service = 
            new OrderProcessingServiceWithPoorShutdown(5);
        
        List<Order> orders = new ArrayList<>();
        
        // Submit multiple orders
        for (int i = 1; i <= 10; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i),
                                  "USER-" + (100 + i),
                                  "PROD-001", 1, 99.99);
            orders.add(order);
            service.processOrder(order);
        }
        
        System.out.println("\n[System] Submitted 10 orders");
        System.out.println("[System] Waiting 1 second, then shutting down abruptly...\n");
        
        Thread.sleep(1000);
        
        // ⚠️  Poor shutdown: Abrupt termination
        service.shutdown();
        
        // Wait a bit to see results
        Thread.sleep(1000);
        
        // Check order statuses
        System.out.println("\n--- Results (Poor Shutdown) ---");
        int completed = 0;
        int failed = 0;
        int processing = 0;
        
        for (Order order : orders) {
            switch (order.getStatus()) {
                case COMPLETED:
                    completed++;
                    System.out.println("✓ " + order.getOrderId() + " completed");
                    break;
                case FAILED:
                case CANCELLED:
                    failed++;
                    System.out.println("✗ " + order.getOrderId() + " " + order.getStatus() + 
                                     " (interrupted during shutdown)");
                    break;
                case PROCESSING:
                    processing++;
                    System.out.println("⚠️  " + order.getOrderId() + " still processing (interrupted)");
                    break;
                case PENDING:
                default:
                    // Order still pending or unknown state
                    break;
            }
        }
        
        System.out.println("\nCompleted: " + completed + "/10");
        System.out.println("Failed/Cancelled: " + failed + "/10");
        System.out.println("Still processing: " + processing + "/10");
        System.out.println("\n⚠️  Problem: Orders interrupted during shutdown");
        System.out.println("⚠️  Data may be in inconsistent state");
        System.out.println("⚠️  Resources may not be properly released");
    }
    
    /**
     * Demonstrates proper shutdown handling.
     * ✅ Tasks complete gracefully, no data loss
     */
    private static void demonstrateProperShutdown() throws InterruptedException {
        System.out.println("Demonstrating proper shutdown (graceful termination)...");
        System.out.println("✅ Tasks complete before shutdown\n");
        
        OrderProcessingService service = new OrderProcessingService(5, 10);
        
        List<Order> orders = new ArrayList<>();
        
        // Submit multiple orders
        for (int i = 1; i <= 10; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i + 10),
                                  "USER-" + (200 + i),
                                  "PROD-001", 1, 99.99);
            orders.add(order);
            service.processOrder(order);
        }
        
        System.out.println("\n[System] Submitted 10 orders");
        System.out.println("[System] Waiting 1 second, then initiating graceful shutdown...\n");
        
        Thread.sleep(1000);
        
        // ✅ Proper shutdown: Graceful termination
        service.gracefulShutdown();
        
        // Check order statuses
        System.out.println("\n--- Results (Proper Shutdown) ---");
        int completed = 0;
        int failed = 0;
        int cancelled = 0;
        
        for (Order order : orders) {
            switch (order.getStatus()) {
                case COMPLETED:
                    completed++;
                    System.out.println("✓ " + order.getOrderId() + " completed");
                    break;
                case FAILED:
                    failed++;
                    System.out.println("✗ " + order.getOrderId() + " failed");
                    break;
                case CANCELLED:
                    cancelled++;
                    System.out.println("⚠️  " + order.getOrderId() + " cancelled (timeout)");
                    break;
                case PENDING:
                case PROCESSING:
                default:
                    // Order still pending or processing
                    break;
            }
        }
        
        System.out.println("\nCompleted: " + completed + "/10");
        System.out.println("Failed: " + failed + "/10");
        System.out.println("Cancelled: " + cancelled + "/10");
        System.out.println("\n✅ All tasks handled gracefully");
        System.out.println("✅ No data loss or inconsistent state");
        System.out.println("✅ Resources properly released");
        
        service.printStatistics();
    }
    
    /**
     * Demonstrates shutdown hook for system shutdown.
     */
    private static void demonstrateShutdownHook() throws InterruptedException {
        System.out.println("Demonstrating shutdown hook...");
        System.out.println("✅ Service will shutdown gracefully even on system shutdown\n");
        
        OrderProcessingService service = new OrderProcessingService(3, 5);
        
        // Submit some orders
        for (int i = 1; i <= 5; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i + 20),
                                  "USER-" + (300 + i),
                                  "PROD-001", 1, 99.99);
            service.processOrder(order);
        }
        
        System.out.println("[System] Submitted 5 orders");
        System.out.println("[System] Service has shutdown hook registered");
        System.out.println("[System] If JVM shuts down (Ctrl+C), service will shutdown gracefully");
        System.out.println("[System] Waiting 2 seconds...\n");
        
        Thread.sleep(2000);
        
        // Manual graceful shutdown (simulating what shutdown hook would do)
        System.out.println("[System] Simulating graceful shutdown (as shutdown hook would do)...");
        service.gracefulShutdown();
        
        System.out.println("\n✅ Shutdown hook ensures graceful shutdown even on system termination");
        
        // Demonstrate timeout scenario
        System.out.println("\n=== Demonstrating Timeout Scenario ===");
        demonstrateTimeoutScenario();
    }
    
    /**
     * Demonstrates timeout scenario.
     */
    private static void demonstrateTimeoutScenario() throws InterruptedException {
        OrderProcessingService service = new OrderProcessingService(2, 2); // 2 second timeout
        
        // Submit long-running tasks
        for (int i = 1; i <= 5; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i + 30),
                                  "USER-" + (400 + i),
                                  "PROD-001", 1, 99.99);
            service.processOrder(order);
        }
        
        System.out.println("[System] Submitted 5 long-running orders");
        System.out.println("[System] Initiating shutdown with 2 second timeout...\n");
        
        // Shutdown with short timeout
        service.gracefulShutdown();
        
        System.out.println("\n✅ Timeout handled: Some tasks may be cancelled if timeout exceeded");
    }
}

