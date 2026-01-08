package com.inventoryProcessing.Concept5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CONCEPT #5 DEMONSTRATION: ExecutorService (Thread Pool)
 * 
 * This demo shows:
 * 1. BEFORE: Manual thread creation (inefficient)
 * 2. AFTER: ExecutorService with thread pool (efficient)
 * 
 * Interview Tip:
 * - ExecutorService is preferred over manual thread management
 * - Thread pools reuse threads (better performance)
 * - FixedThreadPool for CPU-bound tasks
 * - CachedThreadPool for I/O-bound tasks (but can be unbounded)
 * - Always shutdown ExecutorService properly
 */
public class OrderProcessingDemo5 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #5: ExecutorService (Thread Pool) ===\n");
        
        // ============================================
        // PART 1: Manual thread management (BEFORE)
        // ============================================
        System.out.println("--- PART 1: Manual Thread Management (BEFORE) ---\n");
        demonstrateManualThreadManagement();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: ExecutorService (AFTER)
        // ============================================
        System.out.println("\n\n--- PART 2: ExecutorService with Thread Pool (AFTER) ---\n");
        demonstrateExecutorService();
    }
    
    /**
     * Demonstrates manual thread creation and management.
     * ⚠️  INEFFICIENT: Creates new thread for each order
     */
    private static void demonstrateManualThreadManagement() throws InterruptedException {
        System.out.println("Processing 20 orders with manual thread creation...");
        System.out.println("⚠️  Each order gets its own thread (expensive!)\n");
        
        InventoryService inventoryService = new InventoryService();
        OrderProcessingServiceManual service = new OrderProcessingServiceManual(inventoryService);
        
        // Create orders
        List<Order> orders = createOrders(20);
        
        long startTime = System.currentTimeMillis();
        
        // Submit orders (creates new thread for each)
        for (Order order : orders) {
            service.processOrder(order);
            Thread.sleep(10); // Small delay between submissions
        }
        
        System.out.println("\n[System] All orders submitted. Active threads: " + service.getActiveThreadCount());
        System.out.println("[System] Waiting for completion...\n");
        
        // Wait for all threads to complete
        service.waitForCompletion();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Count completed orders
        int completed = (int) orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
            .count();
        
        System.out.println("\n--- Results (Manual Thread Management) ---");
        System.out.println("Orders processed: " + completed + "/" + orders.size());
        System.out.println("Time taken: " + duration + "ms");
        System.out.println("⚠️  Created 20 threads (one per order)");
        System.out.println("⚠️  High overhead: thread creation + destruction");
        System.out.println("⚠️  No thread reuse");
        System.out.println("⚠️  Difficult to manage lifecycle");
    }
    
    /**
     * Demonstrates ExecutorService with thread pool.
     * ✅ EFFICIENT: Reuses threads from pool
     */
    private static void demonstrateExecutorService() throws InterruptedException {
        System.out.println("Processing 20 orders with ExecutorService thread pool...");
        System.out.println("✅ Thread pool size: 5 (reuses threads)\n");
        
        InventoryService inventoryService = new InventoryService();
        int poolSize = 5; // Reuse 5 threads for all orders
        OrderProcessingService service = new OrderProcessingService(inventoryService, poolSize);
        
        // Create orders
        List<Order> orders = createOrders(20);
        
        long startTime = System.currentTimeMillis();
        
        // Submit orders to thread pool (reuses threads)
        for (Order order : orders) {
            service.processOrder(order);
            Thread.sleep(10); // Small delay between submissions
        }
        
        System.out.println("\n[System] All orders submitted to thread pool");
        System.out.println("[System] Waiting for completion...\n");
        
        // Wait for all tasks to complete
        service.waitForCompletion();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Count completed orders
        int completed = (int) orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
            .count();
        
        // Print statistics
        service.printStatistics();
        
        System.out.println("\n--- Results (ExecutorService Thread Pool) ---");
        System.out.println("Orders processed: " + completed + "/" + orders.size());
        System.out.println("Time taken: " + duration + "ms");
        System.out.println("✅ Used only " + poolSize + " threads (reused for all orders)");
        System.out.println("✅ Low overhead: threads created once, reused many times");
        System.out.println("✅ Better resource management");
        System.out.println("✅ Easier lifecycle management");
        
        System.out.println("\n=== Key Benefits of ExecutorService ===");
        System.out.println("1. Thread Reuse: Threads created once, reused for many tasks");
        System.out.println("2. Resource Control: Limit maximum concurrent threads");
        System.out.println("3. Task Queue: Tasks wait in queue when all threads busy");
        System.out.println("4. Lifecycle Management: Easy shutdown and await termination");
        System.out.println("5. Better Performance: No thread creation overhead per task");
        
        System.out.println("\n=== Thread Pool Types ===");
        System.out.println("FixedThreadPool:");
        System.out.println("  - Fixed number of threads");
        System.out.println("  - Good for CPU-bound tasks");
        System.out.println("  - Example: Executors.newFixedThreadPool(10)");
        
        System.out.println("\nCachedThreadPool:");
        System.out.println("  - Creates threads as needed");
        System.out.println("  - Good for I/O-bound tasks");
        System.out.println("  - ⚠️  Can create unlimited threads");
        System.out.println("  - Example: Executors.newCachedThreadPool()");
        
        System.out.println("\nScheduledThreadPool:");
        System.out.println("  - For scheduled/repeated tasks");
        System.out.println("  - Example: Executors.newScheduledThreadPool(5)");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Always shutdown ExecutorService");
        System.out.println("✓ Use awaitTermination() for graceful shutdown");
        System.out.println("✓ Choose pool size based on task type (CPU vs I/O)");
        System.out.println("✓ Use custom ThreadPoolExecutor for fine control");
    }
    
    /**
     * Create sample orders for testing.
     */
    private static List<Order> createOrders(int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String orderId = "ORD-" + String.format("%03d", i);
            String userId = "USER-" + ThreadLocalRandom.current().nextInt(100, 999);
            String productId = "PROD-00" + ThreadLocalRandom.current().nextInt(1, 6);
            int quantity = ThreadLocalRandom.current().nextInt(1, 4);
            double price = ThreadLocalRandom.current().nextDouble(10.0, 500.0);
            
            orders.add(new Order(orderId, userId, productId, quantity, price));
        }
        return orders;
    }
}