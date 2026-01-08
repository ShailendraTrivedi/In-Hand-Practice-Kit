package com.inventoryProcessing.Concept2;

/**
 * CONCEPT #2 DEMONSTRATION: Race Condition & synchronized blocks
 * 
 * This demo shows:
 * 1. BEFORE: Race condition causing incorrect inventory counts
 * 2. AFTER: Synchronized blocks fixing the problem
 * 
 * Interview Tip:
 * - Race conditions are hard to reproduce (non-deterministic)
 * - Run multiple times to see the problem
 * - synchronized ensures mutual exclusion
 * - Performance trade-off: synchronized blocks can cause contention
 */
public class OrderProcessingDemo2 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #2: Race Condition & synchronized blocks ===\n");
        
        // ============================================
        // PART 1: BEFORE - Demonstrating Race Condition
        // ============================================
        System.out.println("--- PART 1: BEFORE (Race Condition Problem) ---");
        demonstrateRaceCondition();
        
        Thread.sleep(2000); // Pause between demos
        
        // ============================================
        // PART 2: AFTER - With Synchronization
        // ============================================
        System.out.println("\n\n--- PART 2: AFTER (With synchronized blocks) ---");
        demonstrateSynchronizedSolution();
    }
    
    /**
     * Demonstrates race condition with UNSAFE inventory service.
     * Multiple threads processing orders for the same product will cause lost updates.
     */
    private static void demonstrateRaceCondition() throws InterruptedException {
        System.out.println("Creating 10 orders for PROD-001 (initial stock: 100, each order: 1 unit)");
        System.out.println("Expected final stock: 90 (100 - 10)\n");
        
        InventoryServiceUnsafe unsafeInventory = new InventoryServiceUnsafe();
        int initialStock = unsafeInventory.getStock("PROD-001");
        System.out.println("Initial stock for PROD-001: " + initialStock);
        
        // Create 10 orders for the same product
        Order[] orders = new Order[10];
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            orders[i] = new Order("ORD-" + String.format("%03d", i+1), 
                                 "USER-" + (100 + i), 
                                 "PROD-001", 
                                 1, 
                                 99.99);
        }
        
        // Process all orders concurrently (race condition!)
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            final int workerId = i;
            Order order = orders[i];
            
            // Create worker that uses UNSAFE inventory
            Thread thread = new Thread(() -> {
                try {
                    System.out.println("[Worker-" + workerId + "] Processing: " + order.getOrderId());
                    
                    // ⚠️  RACE CONDITION: Multiple threads accessing shared state without sync
                    boolean reserved = unsafeInventory.reserveStock(order.getProductId(), order.getQuantity());
                    
                    if (reserved) {
                        int currentStock = unsafeInventory.getStock(order.getProductId());
                        System.out.println("[Worker-" + workerId + "] ✓ Reserved. Current stock: " + currentStock);
                    } else {
                        System.out.println("[Worker-" + workerId + "] ✗ Failed to reserve");
                    }
                } catch (Exception e) {
                    System.err.println("[Worker-" + workerId + "] Error: " + e.getMessage());
                }
            }, "UnsafeWorker-" + i);
            
            threads[i] = thread;
            thread.start();
        }
        
        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        int finalStock = unsafeInventory.getStock("PROD-001");
        
        System.out.println("\n--- Results (UNSAFE) ---");
        System.out.println("Final stock: " + finalStock);
        System.out.println("Expected: 90");
        System.out.println("Difference: " + (90 - finalStock));
        
        if (finalStock != 90) {
            System.out.println("❌ RACE CONDITION DETECTED! Lost " + (90 - finalStock) + " updates.");
            System.out.println("   Some threads read the same value and overwrote each other's updates.");
        } else {
            System.out.println("✓ No race condition this time (but it can still happen!)");
        }
        System.out.println("Processing time: " + (endTime - startTime) + "ms");
    }
    
    /**
     * Demonstrates the solution using synchronized blocks.
     * All inventory operations are now thread-safe.
     */
    private static void demonstrateSynchronizedSolution() throws InterruptedException {
        System.out.println("Creating 10 orders for PROD-001 (initial stock: 100, each order: 1 unit)");
        System.out.println("Expected final stock: 90 (100 - 10)\n");
        
        InventoryService safeInventory = new InventoryService();
        int initialStock = safeInventory.getStock("PROD-001");
        System.out.println("Initial stock for PROD-001: " + initialStock);
        
        // Create 10 orders for the same product
        Order[] orders = new Order[10];
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            orders[i] = new Order("ORD-" + String.format("%03d", i+1), 
                                 "USER-" + (200 + i), 
                                 "PROD-001", 
                                 1, 
                                 99.99);
        }
        
        // Process all orders concurrently (now safe!)
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            OrderWorkerWithInventory worker = new OrderWorkerWithInventory(orders[i], i, safeInventory);
            Thread thread = new Thread(worker, "SafeWorker-" + i);
            threads[i] = thread;
            thread.start();
        }
        
        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        int finalStock = safeInventory.getStock("PROD-001");
        
        System.out.println("\n--- Results (SAFE with synchronized) ---");
        System.out.println("Final stock: " + finalStock);
        System.out.println("Expected: 90");
        System.out.println("Difference: " + (90 - finalStock));
        
        if (finalStock == 90) {
            System.out.println("✅ CORRECT! All updates were applied atomically.");
            System.out.println("   synchronized blocks ensured mutual exclusion.");
        } else {
            System.out.println("❌ Unexpected result (should not happen with proper synchronization)");
        }
        System.out.println("Processing time: " + (endTime - startTime) + "ms");
        
        // Print completed orders
        System.out.println("\n--- Order Status Summary ---");
        int completed = 0;
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                completed++;
            }
        }
        System.out.println("Completed orders: " + completed + "/10");
    }
}