package com.inventoryProcessing.Concept3;

/**
 * CONCEPT #3 DEMONSTRATION: volatile (visibility for order cancellation)
 * 
 * This demo shows:
 * 1. BEFORE: Visibility issue - cancellation might not be detected
 * 2. AFTER: volatile ensures immediate visibility of cancellation
 * 
 * Interview Tip:
 * - volatile solves visibility, not atomicity
 * - Perfect for flags and single-value updates
 * - synchronized also provides visibility (but volatile is lighter for flags)
 * - volatile prevents compiler reordering
 */
public class OrderProcessingDemo3 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #3: volatile (Visibility for Order Cancellation) ===\n");
        
        // ============================================
        // PART 1: Demonstrating the problem (without volatile)
        // ============================================
        System.out.println("--- PART 1: Visibility Issue (Without volatile) ---");
        System.out.println("Note: This might work sometimes, but can fail due to visibility issues\n");
        demonstrateVisibilityIssue();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Solution with volatile
        // ============================================
        System.out.println("\n\n--- PART 2: Solution with volatile ---");
        demonstrateVolatileSolution();
    }
    
    /**
     * Demonstrates visibility issue when cancellation flag is not volatile.
     * Worker thread might not see the cancellation immediately.
     */
    private static void demonstrateVisibilityIssue() throws InterruptedException {
        System.out.println("Creating order and starting processing...");
        
        InventoryService inventoryService = new InventoryService();
        OrderWithoutVolatile order = new OrderWithoutVolatile(
            "ORD-001", "USER-101", "PROD-001", 2, 99.99
        );
        
        // Start worker thread
        Thread workerThread = new Thread(() -> {
            System.out.println("[Worker] Started processing: " + order.getOrderId());
            
            // Simulate long-running processing
            for (int i = 0; i < 10; i++) {
                try {
                    // ⚠️  Check cancellation (might read stale value)
                    if (order.isCancelled()) {
                        System.out.println("[Worker] Detected cancellation at step " + i);
                        return;
                    }
                    
                    System.out.println("[Worker] Processing step " + (i + 1) + "/10");
                    Thread.sleep(100); // Simulate work
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            System.out.println("[Worker] ✗ Completed processing (should have been cancelled!)");
        }, "WorkerThread");
        
        workerThread.start();
        
        // Wait a bit, then cancel the order
        Thread.sleep(300);
        System.out.println("\n[Main] Cancelling order after 300ms...");
        order.cancel();
        System.out.println("[Main] Order cancelled flag set to: " + order.isCancelled());
        
        // Wait for worker to finish
        workerThread.join(2000); // Wait max 2 seconds
        
        System.out.println("\n--- Results (Without volatile) ---");
        System.out.println("Order cancelled: " + order.isCancelled());
        System.out.println("⚠️  Worker might not have detected cancellation immediately");
        System.out.println("   (Due to CPU cache - worker might read stale value)");
    }
    
    /**
     * Demonstrates the solution using volatile.
     * Cancellation is immediately visible to all threads.
     */
    private static void demonstrateVolatileSolution() throws InterruptedException {
        System.out.println("Creating order with volatile cancellation flag...");
        
        InventoryService inventoryService = new InventoryService();
        OrderCancellationService cancellationService = new OrderCancellationService();
        
        // Create multiple orders
        Order[] orders = {
            new Order("ORD-101", "USER-201", "PROD-001", 1, 99.99),
            new Order("ORD-102", "USER-202", "PROD-002", 2, 149.50),
            new Order("ORD-103", "USER-203", "PROD-003", 1, 79.99)
        };
        
        // Register orders
        for (Order order : orders) {
            cancellationService.registerOrder(order);
        }
        
        // Start worker threads
        Thread[] workerThreads = new Thread[orders.length];
        OrderWorkerWithCancellation[] workers = new OrderWorkerWithCancellation[orders.length];
        
        for (int i = 0; i < orders.length; i++) {
            workers[i] = new OrderWorkerWithCancellation(orders[i], i, inventoryService);
            workerThreads[i] = new Thread(workers[i], "Worker-" + i);
            workerThreads[i].start();
        }
        
        // Cancel one order after a short delay
        Thread.sleep(200);
        System.out.println("\n[CancellationService] Cancelling ORD-102...");
        cancellationService.cancelOrder("ORD-102");
        System.out.println("[CancellationService] ✅ Cancellation flag set (volatile - immediately visible)");
        
        // Cancel another order
        Thread.sleep(300);
        System.out.println("\n[CancellationService] Cancelling ORD-103...");
        cancellationService.cancelOrder("ORD-103");
        
        // Wait for all workers to finish
        for (Thread thread : workerThreads) {
            thread.join();
        }
        
        // Print results
        System.out.println("\n--- Results (With volatile) ---");
        for (Order order : orders) {
            System.out.println(order);
            if (order.isCancelled() && order.getStatus() == Order.OrderStatus.CANCELLED) {
                System.out.println("  ✅ Cancellation was detected and handled correctly");
            } else if (!order.isCancelled() && order.getStatus() == Order.OrderStatus.COMPLETED) {
                System.out.println("  ✅ Order completed successfully");
            }
        }
        
        System.out.println("\n=== Key Takeaways ===");
        System.out.println("1. volatile ensures immediate visibility across threads");
        System.out.println("2. All reads/writes go directly to main memory");
        System.out.println("3. Perfect for flags and single-value updates");
        System.out.println("4. Worker threads immediately see cancellation");
        System.out.println("5. No wasted processing on cancelled orders");
        
        System.out.println("\n=== volatile vs synchronized ===");
        System.out.println("volatile:");
        System.out.println("  - Provides visibility only");
        System.out.println("  - Lighter weight (no locking)");
        System.out.println("  - Perfect for flags");
        System.out.println("synchronized:");
        System.out.println("  - Provides visibility AND mutual exclusion");
        System.out.println("  - Heavier (acquires lock)");
        System.out.println("  - Needed for compound operations");
    }
}