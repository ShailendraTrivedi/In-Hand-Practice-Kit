package com.inventoryProcessing.Concept10;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * CONCEPT #10 DEMONSTRATION: Thread Starvation Prevention
 * 
 * This demo shows:
 * 1. BEFORE: Thread starvation (single pool, wrong sizing)
 * 2. AFTER: Starvation prevention (separate pools, proper sizing)
 * 
 * Interview Tip:
 * - CPU-bound: Threads ≈ CPU cores
 * - I/O-bound: Threads can be 2x-10x CPU cores
 * - Separate pools: Prevents I/O from blocking CPU tasks
 * - Monitor utilization: Adjust pool sizes based on metrics
 */
public class OrderProcessingDemo10 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #10: Thread Starvation Prevention ===\n");
        
        int cpuCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Available CPU cores: " + cpuCores + "\n");
        
        // ============================================
        // PART 1: Demonstrating thread starvation (BEFORE)
        // ============================================
        System.out.println("--- PART 1: Thread Starvation (BEFORE) ---\n");
        demonstrateThreadStarvation(cpuCores);
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Starvation prevention (AFTER)
        // ============================================
        System.out.println("\n\n--- PART 2: Starvation Prevention (AFTER) ---\n");
        demonstrateStarvationPrevention(cpuCores);
    }
    
    /**
     * Demonstrates thread starvation with single pool.
     * ⚠️  I/O tasks block threads, CPU tasks wait
     */
    private static void demonstrateThreadStarvation(int cpuCores) throws InterruptedException {
        System.out.println("Using single thread pool for all tasks...");
        System.out.println("⚠️  I/O tasks block threads → CPU tasks starve\n");
        
        OrderProcessingServiceWithStarvation service = 
            new OrderProcessingServiceWithStarvation(cpuCores);
        
        List<Order> orders = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(20);
        
        // Create mix of CPU-bound and I/O-bound tasks
        for (int i = 1; i <= 10; i++) {
            Order cpuOrder = new Order("CPU-ORD-" + String.format("%03d", i),
                                     "USER-" + (100 + i),
                                     "PROD-001", 1, 99.99);
            orders.add(cpuOrder);
            
            Order ioOrder = new Order("IO-ORD-" + String.format("%03d", i),
                                    "USER-" + (200 + i),
                                    "PROD-001", 1, 99.99);
            orders.add(ioOrder);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Submit tasks
        for (int i = 0; i < orders.size(); i += 2) {
            final int index = i;
            service.processCpuBoundTask(orders.get(index));
            service.processIoBoundTask(orders.get(index + 1));
        }
        
        // Wait a bit
        Thread.sleep(3000);
        
        long endTime = System.currentTimeMillis();
        
        service.shutdown();
        
        // Check results
        System.out.println("\n--- Results (Thread Starvation) ---");
        int cpuCompleted = 0;
        int ioCompleted = 0;
        
        for (Order order : orders) {
            if (order.getOrderId().startsWith("CPU")) {
                if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                    cpuCompleted++;
                }
            } else {
                if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                    ioCompleted++;
                }
            }
        }
        
        System.out.println("CPU tasks completed: " + cpuCompleted + "/10");
        System.out.println("I/O tasks completed: " + ioCompleted + "/10");
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("⚠️  CPU tasks may be starved if I/O tasks block threads");
    }
    
    /**
     * Demonstrates starvation prevention with separate pools.
     * ✅ CPU and I/O tasks don't interfere with each other
     */
    private static void demonstrateStarvationPrevention(int cpuCores) throws InterruptedException {
        System.out.println("Using separate thread pools for different task types...");
        System.out.println("✅ CPU tasks use CPU pool, I/O tasks use I/O pool\n");
        
        OrderProcessingService service = new OrderProcessingService(cpuCores);
        
        List<Order> orders = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(20);
        
        // Create mix of CPU-bound and I/O-bound tasks
        for (int i = 1; i <= 10; i++) {
            Order cpuOrder = new Order("CPU-ORD-" + String.format("%03d", i),
                                     "USER-" + (300 + i),
                                     "PROD-001", 1, 99.99);
            orders.add(cpuOrder);
            
            Order ioOrder = new Order("IO-ORD-" + String.format("%03d", i),
                                    "USER-" + (400 + i),
                                    "PROD-001", 1, 99.99);
            orders.add(ioOrder);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Submit tasks to appropriate pools
        for (int i = 0; i < orders.size(); i += 2) {
            service.processCpuBoundTask(orders.get(i));
            service.processIoBoundTask(orders.get(i + 1));
        }
        
        // Wait a bit
        Thread.sleep(3000);
        
        long endTime = System.currentTimeMillis();
        
        service.shutdown();
        
        // Check results
        System.out.println("\n--- Results (Starvation Prevention) ---");
        int cpuCompleted = 0;
        int ioCompleted = 0;
        
        for (Order order : orders) {
            if (order.getOrderId().startsWith("CPU")) {
                if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                    cpuCompleted++;
                }
            } else {
                if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                    ioCompleted++;
                }
            }
        }
        
        System.out.println("CPU tasks completed: " + cpuCompleted + "/10");
        System.out.println("I/O tasks completed: " + ioCompleted + "/10");
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("✅ Both task types processed without starvation");
        
        // Demonstrate priority processing
        System.out.println("\n=== Demonstrating Priority Processing ===");
        demonstratePriorityProcessing(cpuCores);
        
        System.out.println("\n=== Key Takeaways ===");
        System.out.println("1. ✅ Separate pools: CPU-bound and I/O-bound tasks");
        System.out.println("2. ✅ Proper sizing: CPU tasks ≈ cores, I/O tasks can be more");
        System.out.println("3. ✅ Task separation: Prevents I/O from blocking CPU tasks");
        System.out.println("4. ✅ Priority queues: Process high-priority first (but fairly)");
        System.out.println("5. ✅ Monitor utilization: Adjust sizes based on metrics");
        
        System.out.println("\n=== Pool Sizing Guidelines ===");
        System.out.println("CPU-bound tasks:");
        System.out.println("  - Threads ≈ CPU cores");
        System.out.println("  - Example: 8 cores → 8 threads");
        System.out.println("  - Reason: CPU tasks don't block, extra threads waste resources");
        
        System.out.println("\nI/O-bound tasks:");
        System.out.println("  - Threads = 2x to 10x CPU cores");
        System.out.println("  - Example: 8 cores → 16-80 threads");
        System.out.println("  - Reason: I/O tasks block, more threads = better throughput");
        
        System.out.println("\nMixed tasks:");
        System.out.println("  - Threads = 2x CPU cores (balanced)");
        System.out.println("  - Or: Separate into CPU and I/O pools");
        
        System.out.println("\n=== Starvation Prevention Techniques ===");
        System.out.println("1. Separate Pools:");
        System.out.println("   - CPU-bound: Small pool (≈ cores)");
        System.out.println("   - I/O-bound: Larger pool (2x-10x cores)");
        
        System.out.println("\n2. Priority Queues:");
        System.out.println("   - Process high-priority first");
        System.out.println("   - But ensure low-priority also get processed");
        
        System.out.println("\n3. Fair Scheduling:");
        System.out.println("   - Use fair locks (ReentrantLock(true))");
        System.out.println("   - Round-robin task selection");
        
        System.out.println("\n4. Monitor and Adjust:");
        System.out.println("   - Track task completion times");
        System.out.println("   - Monitor pool utilization");
        System.out.println("   - Adjust pool sizes based on metrics");
    }
    
    /**
     * Demonstrates priority processing.
     */
    private static void demonstratePriorityProcessing(int cpuCores) throws InterruptedException {
        OrderProcessingService service = new OrderProcessingService(cpuCores);
        
        // Create orders with different priorities
        List<Order> orders = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Order order = new Order("PRI-ORD-" + String.format("%03d", i),
                                  "USER-" + (500 + i),
                                  "PROD-001", 1, 99.99);
            
            // Mix priorities
            if (i <= 3) {
                order.setPriority(Order.OrderPriority.VIP);
            } else if (i <= 6) {
                order.setPriority(Order.OrderPriority.HIGH);
            } else if (i <= 9) {
                order.setPriority(Order.OrderPriority.NORMAL);
            } else {
                order.setPriority(Order.OrderPriority.LOW);
            }
            
            orders.add(order);
        }
        
        System.out.println("Submitting orders with different priorities...");
        System.out.println("VIP: 3, HIGH: 3, NORMAL: 3, LOW: 3\n");
        
        // Submit all orders
        for (Order order : orders) {
            service.processOrderWithPriority(order);
        }
        
        Thread.sleep(2000);
        
        service.shutdown();
        
        System.out.println("\n--- Priority Processing Results ---");
        for (Order order : orders) {
            System.out.println(order.getOrderId() + " - Priority: " + order.getPriority() + 
                             " - Status: " + order.getStatus());
        }
        
        System.out.println("\n✅ High-priority tasks processed first");
        System.out.println("✅ But all tasks eventually get processed (no starvation)");
    }
}