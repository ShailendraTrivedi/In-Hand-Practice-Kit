package com.inventoryProcessing.Concept1;

/**
 * CONCEPT #1 DEMONSTRATION: Thread & Runnable
 * 
 * This class demonstrates:
 * 1. Creating Runnable tasks (OrderWorker)
 * 2. Creating Thread objects to execute those tasks
 * 3. Starting threads to run concurrently
 * 4. Using join() to wait for thread completion
 * 
 * Interview Tip:
 * - Always remember: Thread.start() starts a new thread, run() executes in current thread
 * - join() is used to wait for a thread to complete
 * - Each thread has its own stack and program counter
 */
public class OrderProcessingDemo1 {
    
    public static void main(String[] args) {
        System.out.println("=== CONCEPT #1: Thread & Runnable Demo ===\n");
        System.out.println("Demonstrating concurrent order processing using basic Thread & Runnable\n");
        
        // Create multiple orders
        Order[] orders = {
            new Order("ORD-001", "USER-101", "PROD-001", 2, 99.99),
            new Order("ORD-002", "USER-102", "PROD-002", 1, 149.50),
            new Order("ORD-003", "USER-103", "PROD-003", 3, 79.99),
            new Order("ORD-004", "USER-104", "PROD-001", 1, 99.99),
            new Order("ORD-005", "USER-105", "PROD-004", 2, 199.99)
        };
        
        // BEFORE: Sequential Processing (Old Approach)
        System.out.println("--- BEFORE: Sequential Processing (One at a time) ---");
        long startSequential = System.currentTimeMillis();
        for (int i = 0; i < orders.length; i++) {
            OrderWorker worker = new OrderWorker(orders[i], i);
            worker.run(); // ❌ WRONG: This runs in the main thread (sequential)
        }
        long timeSequential = System.currentTimeMillis() - startSequential;
        System.out.println("Sequential processing time: " + timeSequential + "ms\n");
        
        // Reset orders for concurrent demo
        for (Order order : orders) {
            order.setStatus(Order.OrderStatus.PENDING);
        }
        
        // AFTER: Concurrent Processing (New Approach with Thread & Runnable)
        System.out.println("--- AFTER: Concurrent Processing (Using Thread & Runnable) ---");
        long startConcurrent = System.currentTimeMillis();
        
        // Create Thread array to hold all worker threads
        Thread[] threads = new Thread[orders.length];
        
        // Create and start threads for each order
        for (int i = 0; i < orders.length; i++) {
            // Step 1: Create Runnable task
            OrderWorker worker = new OrderWorker(orders[i], i);
            
            // Step 2: Create Thread and pass Runnable to it
            Thread thread = new Thread(worker, "OrderThread-" + i);
            
            // Step 3: Start the thread (this begins concurrent execution)
            thread.start();
            
            // Store thread reference for later join()
            threads[i] = thread;
        }
        
        // Step 4: Wait for all threads to complete using join()
        // This ensures main thread waits until all order processing is done
        for (Thread thread : threads) {
            try {
                thread.join(); // Wait for this thread to finish
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted while waiting");
                Thread.currentThread().interrupt();
            }
        }
        
        long timeConcurrent = System.currentTimeMillis() - startConcurrent;
        System.out.println("\nConcurrent processing time: " + timeConcurrent + "ms");
        System.out.println("Performance improvement: " + 
            String.format("%.1f%% faster", ((double)(timeSequential - timeConcurrent) / timeSequential) * 100));
        
        // Print final order statuses
        System.out.println("\n=== Final Order Statuses ===");
        for (Order order : orders) {
            System.out.println(order);
        }
        
        System.out.println("\n=== Key Takeaways ===");
        System.out.println("1. Runnable defines WHAT to do (the task)");
        System.out.println("2. Thread defines HOW to execute it (in a separate thread)");
        System.out.println("3. thread.start() begins concurrent execution");
        System.out.println("4. thread.join() waits for completion");
        System.out.println("5. Concurrent execution improves throughput significantly");
        System.out.println("\n⚠️  NOTE: We haven't handled shared state yet!");
        System.out.println("   In real systems, multiple threads accessing shared data");
        System.out.println("   (like inventory) can cause race conditions.");
        System.out.println("   We'll fix this in Concept #2: synchronized blocks");
    }
}