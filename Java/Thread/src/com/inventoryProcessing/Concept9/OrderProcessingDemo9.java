package com.inventoryProcessing.Concept9;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CONCEPT #9 DEMONSTRATION: Deadlock Prevention (Lock Ordering / Design)
 * 
 * This demo shows:
 * 1. BEFORE: Deadlock scenario (circular wait)
 * 2. AFTER: Deadlock prevention (lock ordering, timeout, lock-free)
 * 
 * Interview Tip:
 * - Deadlock requires 4 conditions: Mutual exclusion, Hold and wait, No preemption, Circular wait
 * - Prevention: Break any one condition
 * - Lock ordering: Always acquire locks in same order
 * - Timeout locks: Use tryLock() to detect deadlocks
 * - Lock-free: Use thread-safe collections when possible
 */
public class OrderProcessingDemo9 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #9: Deadlock Prevention (Lock Ordering / Design) ===\n");
        
        // ============================================
        // PART 1: Demonstrating deadlock (BEFORE)
        // ============================================
        System.out.println("--- PART 1: Deadlock Scenario (BEFORE) ---\n");
        demonstrateDeadlock();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Deadlock prevention (AFTER)
        // ============================================
        System.out.println("\n\n--- PART 2: Deadlock Prevention (AFTER) ---\n");
        demonstrateDeadlockPrevention();
    }
    
    /**
     * Demonstrates deadlock scenario.
     * ⚠️  Threads can deadlock when acquiring locks in different order
     */
    private static void demonstrateDeadlock() throws InterruptedException {
        System.out.println("Creating deadlock scenario...");
        System.out.println("⚠️  Thread 1: Locks inventory → waits for payment");
        System.out.println("⚠️  Thread 2: Locks payment → waits for inventory");
        System.out.println("⚠️  Result: Both threads wait forever → DEADLOCK\n");
        
        InventoryService inventoryService = new InventoryService();
        PaymentService paymentService = new PaymentService();
        OrderProcessingServiceWithDeadlock service = new OrderProcessingServiceWithDeadlock(
            inventoryService, paymentService);
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        
        // Thread 1: Uses Method1 (inventory first, then payment)
        executor.submit(() -> {
            try {
                Order order1 = new Order("ORD-001", "USER-101", "PROD-001", 1, 99.99);
                service.processOrderMethod1(order1);
            } finally {
                latch.countDown();
            }
        });
        
        // Small delay to increase chance of deadlock
        Thread.sleep(10);
        
        // Thread 2: Uses Method2 (payment first, then inventory)
        executor.submit(() -> {
            try {
                Order order2 = new Order("ORD-002", "USER-102", "PROD-002", 1, 149.50);
                service.processOrderMethod2(order2);
            } finally {
                latch.countDown();
            }
        });
        
        // Wait with timeout (deadlock will cause timeout)
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        
        if (!completed) {
            System.out.println("\n❌ DEADLOCK DETECTED!");
            System.out.println("   Threads are blocked waiting for each other");
            System.out.println("   System is frozen - this is a deadlock");
        } else {
            System.out.println("\n✓ No deadlock this time (timing-dependent)");
            System.out.println("   Deadlock may occur with different timing");
        }
        
        executor.shutdownNow();
        
        System.out.println("\n--- Deadlock Conditions ---");
        System.out.println("1. ✅ Mutual Exclusion: Locks prevent concurrent access");
        System.out.println("2. ✅ Hold and Wait: Threads hold one lock, wait for another");
        System.out.println("3. ✅ No Preemption: Locks cannot be forcibly taken");
        System.out.println("4. ✅ Circular Wait: Thread 1 waits for Thread 2, Thread 2 waits for Thread 1");
        System.out.println("\nAll 4 conditions met → DEADLOCK");
    }
    
    /**
     * Demonstrates deadlock prevention techniques.
     * ✅ Shows lock ordering, timeout, and lock-free solutions
     */
    private static void demonstrateDeadlockPrevention() throws InterruptedException {
        System.out.println("Demonstrating deadlock prevention techniques...\n");
        
        InventoryService inventoryService = new InventoryService();
        PaymentService paymentService = new PaymentService();
        OrderProcessingService service = new OrderProcessingService(inventoryService, paymentService);
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Order> orders = new ArrayList<>();
        
        // Create orders
        for (int i = 1; i <= 10; i++) {
            orders.add(new Order("ORD-" + String.format("%03d", i + 10),
                               "USER-" + (200 + i),
                               "PROD-00" + ((i % 5) + 1),
                               1,
                               99.99));
        }
        
        // ============================================
        // SOLUTION 1: Lock Ordering
        // ============================================
        System.out.println("=== SOLUTION 1: Lock Ordering ===");
        System.out.println("✅ Always acquire locks in the same order\n");
        
        CountDownLatch latch1 = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            final Order order = orders.get(i);
            executor.submit(() -> {
                try {
                    service.processOrderWithLockOrdering(order);
                } finally {
                    latch1.countDown();
                }
            });
        }
        
        latch1.await();
        System.out.println("✓ Lock ordering: All orders processed without deadlock\n");
        
        Thread.sleep(500);
        
        // ============================================
        // SOLUTION 2: Timeout-based Locks
        // ============================================
        System.out.println("=== SOLUTION 2: Timeout-based Locks ===");
        System.out.println("✅ Use tryLock() with timeout to detect deadlocks\n");
        
        CountDownLatch latch2 = new CountDownLatch(5);
        for (int i = 5; i < 10; i++) {
            final Order order = orders.get(i);
            executor.submit(() -> {
                try {
                    service.processOrderWithTimeout(order, 100);
                } finally {
                    latch2.countDown();
                }
            });
        }
        
        latch2.await();
        System.out.println("✓ Timeout locks: Deadlocks detected and handled\n");
        
        Thread.sleep(500);
        
        // ============================================
        // SOLUTION 3: Lock-free Design
        // ============================================
        System.out.println("=== SOLUTION 3: Lock-free Design ===");
        System.out.println("✅ Use thread-safe collections (no explicit locks)\n");
        
        List<Order> lockFreeOrders = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            lockFreeOrders.add(new Order("ORD-" + String.format("%03d", i + 20),
                                        "USER-" + (300 + i),
                                        "PROD-00" + ((i % 5) + 1),
                                        1,
                                        99.99));
        }
        
        CountDownLatch latch3 = new CountDownLatch(5);
        for (Order order : lockFreeOrders) {
            executor.submit(() -> {
                try {
                    service.processOrderLockFree(order);
                } finally {
                    latch3.countDown();
                }
            });
        }
        
        latch3.await();
        System.out.println("✓ Lock-free: No locks needed, no deadlock risk\n");
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Count results
        int completed = 0;
        int failed = 0;
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                completed++;
            } else if (order.getStatus() == Order.OrderStatus.FAILED) {
                failed++;
            }
        }
        
        System.out.println("=== Final Statistics ===");
        System.out.println("Completed: " + completed);
        System.out.println("Failed: " + failed);
        
        System.out.println("\n=== Deadlock Prevention Techniques ===");
        System.out.println("1. Lock Ordering:");
        System.out.println("   - Always acquire locks in the same order");
        System.out.println("   - Breaks circular wait condition");
        System.out.println("   - Example: Always lock inventory before payment");
        
        System.out.println("\n2. Timeout-based Locks:");
        System.out.println("   - Use tryLock(timeout) instead of lock()");
        System.out.println("   - Detects potential deadlocks");
        System.out.println("   - Allows retry or alternative path");
        
        System.out.println("\n3. Lock-free Design:");
        System.out.println("   - Use thread-safe collections (ConcurrentHashMap)");
        System.out.println("   - Avoid explicit locks when possible");
        System.out.println("   - Best performance, no deadlock risk");
        
        System.out.println("\n4. Minimize Lock Scope:");
        System.out.println("   - Hold locks for shortest time possible");
        System.out.println("   - Release locks before calling external code");
        System.out.println("   - Reduces chance of deadlock");
        
        System.out.println("\n5. Avoid Nested Locks:");
        System.out.println("   - Design to avoid needing multiple locks");
        System.out.println("   - Use single lock when possible");
        System.out.println("   - Consider restructuring code");
        
        System.out.println("\n=== Interview Tips ===");
        System.out.println("✓ Always acquire locks in consistent order");
        System.out.println("✓ Use tryLock() with timeout for deadlock detection");
        System.out.println("✓ Prefer lock-free designs when possible");
        System.out.println("✓ Minimize lock scope and number of locks");
        System.out.println("✓ Test with high concurrency to find deadlocks");
    }
}

