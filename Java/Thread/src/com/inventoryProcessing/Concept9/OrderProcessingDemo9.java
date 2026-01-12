package com.inventoryProcessing.Concept9;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CONCEPT #9 DEMONSTRATION: Deadlock Prevention (Lock Ordering)
 * 
 * This demo shows:
 * 1. BEFORE: Deadlock scenario (different lock orders)
 * 2. AFTER: Deadlock prevention (consistent lock ordering)
 * 
 * Interview Tip:
 * - Always acquire locks in the same order
 * - Use consistent lock hierarchy
 * - Consider timeout-based locks (tryLock)
 * - Design to minimize lock dependencies
 * - Detect deadlocks: Use thread dumps, monitoring tools
 */
public class OrderProcessingDemo9 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #9: Deadlock Prevention (Lock Ordering) ===\n");
        
        // ============================================
        // PART 1: Demonstrating deadlock (BEFORE)
        // ============================================
        System.out.println("--- PART 1: Deadlock Scenario (BEFORE) ---\n");
        System.out.println("⚠️  WARNING: This may cause deadlock!");
        System.out.println("Threads use different lock orders → Circular wait\n");
        
        demonstrateDeadlock();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Deadlock prevention (AFTER)
        // ============================================
        System.out.println("\n\n--- PART 2: Deadlock Prevention (AFTER) ---\n");
        System.out.println("✅ All threads use same lock order → No deadlock\n");
        
        demonstrateDeadlockPrevention();
    }
    
    /**
     * Demonstrates deadlock scenario.
     * ⚠️  Different lock orders cause deadlock
     */
    private static void demonstrateDeadlock() throws InterruptedException {
        InventoryService inventoryService = new InventoryService();
        PaymentService paymentService = new PaymentService();
        
        List<Order> orders = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        
        // Create orders with different lock orders
        for (int i = 1; i <= 4; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i),
                                  "USER-" + (100 + i),
                                  "PROD-00" + ((i % 5) + 1),
                                  1,
                                  99.99);
            orders.add(order);
            
            // Alternate lock orders: Thread 1 uses order 1, Thread 2 uses order 2, etc.
            // This creates the deadlock condition
            boolean lockOrder1 = (i % 2 == 1);
            
            OrderProcessorWithDeadlock processor = new OrderProcessorWithDeadlock(
                order, inventoryService, paymentService, i, lockOrder1);
            
            Thread thread = new Thread(processor, "DeadlockWorker-" + i);
            threads.add(thread);
        }
        
        System.out.println("Starting threads with different lock orders...");
        System.out.println("Thread 1, 3: Inventory → Payment");
        System.out.println("Thread 2, 4: Payment → Inventory");
        System.out.println("This creates circular wait → DEADLOCK!\n");
        
        // Start all threads simultaneously
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for threads (they may deadlock)
        System.out.println("Waiting for threads to complete (may deadlock)...\n");
        
        for (Thread thread : threads) {
            thread.join(3000); // Wait max 3 seconds
        }
        
        // Check which threads are still running (deadlocked)
        System.out.println("\n--- Results (Deadlock Scenario) ---");
        int completed = 0;
        int deadlocked = 0;
        
        for (int i = 0; i < threads.size(); i++) {
            Thread thread = threads.get(i);
            Order order = orders.get(i);
            
            if (thread.isAlive()) {
                deadlocked++;
                System.out.println("❌ " + thread.getName() + " is DEADLOCKED!");
                System.out.println("   Order: " + order.getOrderId() + " stuck in: " + order.getStatus());
            } else {
                completed++;
                System.out.println("✓ " + thread.getName() + " completed");
                System.out.println("   Order: " + order.getOrderId() + " status: " + order.getStatus());
            }
        }
        
        System.out.println("\nCompleted: " + completed + ", Deadlocked: " + deadlocked);
        
        if (deadlocked > 0) {
            System.out.println("\n⚠️  DEADLOCK DETECTED!");
            System.out.println("Cause: Different lock orders create circular wait");
            System.out.println("Solution: Use consistent lock ordering (see Part 2)");
        }
    }
    
    /**
     * Demonstrates deadlock prevention with consistent lock ordering.
     * ✅ All threads use same lock order → No deadlock
     */
    private static void demonstrateDeadlockPrevention() throws InterruptedException {
        InventoryService inventoryService = new InventoryService();
        PaymentService paymentService = new PaymentService();
        
        List<Order> orders = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(10);
        
        // Create orders
        for (int i = 1; i <= 10; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i + 10),
                                  "USER-" + (200 + i),
                                  "PROD-00" + ((i % 5) + 1),
                                  1,
                                  99.99);
            orders.add(order);
            
            // ✅ All threads use the same lock order
            OrderProcessorWithLockOrdering processor = new OrderProcessorWithLockOrdering(
                order, inventoryService, paymentService, i);
            
            executor.submit(() -> {
                try {
                    processor.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        System.out.println("Starting 10 threads with consistent lock order...");
        System.out.println("All threads: Inventory → Payment");
        System.out.println("No circular wait → NO DEADLOCK!\n");
        
        // Wait for all tasks
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Check results
        System.out.println("\n--- Results (Deadlock Prevention) ---");
        int success = 0;
        int failed = 0;
        
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                success++;
                System.out.println("✓ " + order.getOrderId() + " completed");
            } else {
                failed++;
                System.out.println("✗ " + order.getOrderId() + " " + order.getStatus());
            }
        }
        
        System.out.println("\nCompleted: " + success + ", Failed: " + failed);
        System.out.println("Deadlocked: 0 (consistent lock ordering prevents deadlock)");
        
        System.out.println("\n=== Key Takeaways ===");
        System.out.println("1. ✅ Always acquire locks in the same order");
        System.out.println("2. ✅ Define a consistent lock hierarchy");
        System.out.println("3. ✅ All threads must follow the same order");
        System.out.println("4. ✅ Prevents circular wait condition");
        System.out.println("5. ✅ Alternative: Use timeout-based locks (tryLock)");
        
        System.out.println("\n=== Deadlock Prevention Techniques ===");
        System.out.println("1. Lock Ordering:");
        System.out.println("   - Always acquire locks in consistent order");
        System.out.println("   - Example: Always lock Inventory → Payment");
        
        System.out.println("\n2. Timeout-based Locks:");
        System.out.println("   - Use ReentrantLock.tryLock(timeout)");
        System.out.println("   - Release locks if timeout occurs");
        System.out.println("   - Prevents indefinite waiting");
        
        System.out.println("\n3. Lock-free Design:");
        System.out.println("   - Use lock-free data structures");
        System.out.println("   - Use atomic operations");
        System.out.println("   - Minimize lock dependencies");
        
        System.out.println("\n4. Resource Ordering:");
        System.out.println("   - Order locks by resource identifier");
        System.out.println("   - Ensures consistent ordering dynamically");
        
        System.out.println("\n=== Deadlock Detection ===");
        System.out.println("- Thread dumps: jstack, jcmd");
        System.out.println("- Monitoring tools: VisualVM, JProfiler");
        System.out.println("- Look for: 'BLOCKED' threads waiting for locks");
    }
}