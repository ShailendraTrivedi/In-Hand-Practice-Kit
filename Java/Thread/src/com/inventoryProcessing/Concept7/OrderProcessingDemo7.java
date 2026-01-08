package com.inventoryProcessing.Concept7;

import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT #7 DEMONSTRATION: Interrupts (Graceful Cancellation & Shutdown)
 * 
 * This demo shows:
 * 1. BEFORE: Ignoring interrupts (threads cannot be stopped)
 * 2. AFTER: Proper interrupt handling (graceful cancellation)
 * 
 * Interview Tip:
 * - Interruption is cooperative (thread must check and respond)
 * - Always restore interrupt flag: Thread.currentThread().interrupt()
 * - Check interrupt status in loops: while (!Thread.interrupted())
 * - Don't swallow InterruptedException
 * - Use interrupt for graceful shutdown
 */
public class OrderProcessingDemo7 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #7: Interrupts (Graceful Cancellation & Shutdown) ===\n");
        
        // ============================================
        // PART 1: Demonstrating ignoring interrupts (BEFORE)
        // ============================================
        System.out.println("--- PART 1: Ignoring Interrupts (BEFORE - Bad Practice) ---\n");
        demonstrateIgnoringInterrupts();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Proper interrupt handling (AFTER)
        // ============================================
        System.out.println("\n\n--- PART 2: Proper Interrupt Handling (AFTER - Good Practice) ---\n");
        demonstrateProperInterruptHandling();
    }
    
    /**
     * Demonstrates what happens when interrupts are ignored.
     * ⚠️  Threads cannot be stopped gracefully
     */
    private static void demonstrateIgnoringInterrupts() throws InterruptedException {
        System.out.println("Starting workers that ignore interrupts...");
        System.out.println("⚠️  These threads cannot be stopped gracefully\n");
        
        InventoryService inventoryService = new InventoryService();
        List<Thread> threads = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        
        // Create and start workers
        for (int i = 1; i <= 3; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i), 
                                  "USER-" + (100 + i), 
                                  "PROD-001", 
                                  1, 
                                  99.99);
            orders.add(order);
            
            OrderWorkerIgnoringInterrupts worker = new OrderWorkerIgnoringInterrupts(
                order, i, inventoryService);
            Thread thread = new Thread(worker, "BadWorker-" + i);
            threads.add(thread);
            thread.start();
        }
        
        // Wait a bit, then try to interrupt
        Thread.sleep(200);
        System.out.println("\n[Main] Attempting to interrupt workers...");
        
        for (Thread thread : threads) {
            thread.interrupt(); // Set interrupt flag
        }
        
        System.out.println("[Main] Interrupt flags set. Waiting to see if threads stop...\n");
        
        // Wait for threads (they might not stop!)
        for (Thread thread : threads) {
            thread.join(1000); // Wait max 1 second
        }
        
        // Check which threads are still running
        System.out.println("\n--- Results (Ignoring Interrupts) ---");
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                System.out.println("❌ " + thread.getName() + " is still running!");
                System.out.println("   Thread ignored interrupt and continued processing");
            } else {
                System.out.println("✓ " + thread.getName() + " stopped");
            }
        }
        
        System.out.println("\n⚠️  Problem: Threads that ignore interrupts:");
        System.out.println("  - Cannot be stopped gracefully");
        System.out.println("  - Block shutdown process");
        System.out.println("  - Waste resources");
        System.out.println("  - May cause system to hang");
    }
    
    /**
     * Demonstrates proper interrupt handling.
     * ✅ Threads stop gracefully when interrupted
     */
    private static void demonstrateProperInterruptHandling() throws InterruptedException {
        System.out.println("Starting workers with proper interrupt handling...");
        System.out.println("✅ These threads respond to interrupts gracefully\n");
        
        InventoryService inventoryService = new InventoryService();
        OrderProcessingService service = new OrderProcessingService(inventoryService, 5);
        
        List<Thread> threads = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        
        // Create and start workers
        for (int i = 1; i <= 5; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i + 10), 
                                  "USER-" + (200 + i), 
                                  "PROD-00" + ((i % 5) + 1), 
                                  1, 
                                  99.99);
            orders.add(order);
            
            Thread thread = service.processOrderWithThread(order, i);
            if (thread != null) {
                threads.add(thread);
            }
        }
        
        // Wait a bit, then interrupt some threads
        Thread.sleep(300);
        System.out.println("\n[Main] Interrupting workers 1, 2, and 3...");
        
        // Interrupt specific threads
        for (int i = 0; i < 3 && i < threads.size(); i++) {
            Thread thread = threads.get(i);
            if (thread.isAlive()) {
                System.out.println("[Main] Interrupting: " + thread.getName());
                thread.interrupt(); // ✅ Set interrupt flag
            }
        }
        
        System.out.println("[Main] Waiting for threads to respond to interrupts...\n");
        
        // Wait for all threads
        service.waitForThreads(3000);
        
        // Check results
        System.out.println("\n--- Results (Proper Interrupt Handling) ---");
        int cancelled = 0;
        int completed = 0;
        int failed = 0;
        
        for (Order order : orders) {
            switch (order.getStatus()) {
                case CANCELLED:
                    cancelled++;
                    System.out.println("✓ " + order.getOrderId() + " was cancelled gracefully");
                    break;
                case COMPLETED:
                    completed++;
                    System.out.println("✓ " + order.getOrderId() + " completed successfully");
                    break;
                case FAILED:
                    failed++;
                    System.out.println("✗ " + order.getOrderId() + " failed");
                    break;
                case PENDING:
                case PROCESSING:
                default:
                    // Order still processing or in unknown state
                    break;
            }
        }
        
        System.out.println("\n=== Statistics ===");
        System.out.println("Cancelled (interrupted): " + cancelled);
        System.out.println("Completed: " + completed);
        System.out.println("Failed: " + failed);
        
        // Check thread status
        System.out.println("\n=== Thread Status ===");
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                System.out.println("⚠️  " + thread.getName() + " is still alive");
            } else {
                System.out.println("✓ " + thread.getName() + " terminated");
            }
        }
        
        // Demonstrate graceful shutdown
        System.out.println("\n=== Demonstrating Graceful Shutdown ===");
        demonstrateGracefulShutdown();
        
        System.out.println("\n=== Key Takeaways ===");
        System.out.println("1. ✅ Always check interrupt status: Thread.currentThread().isInterrupted()");
        System.out.println("2. ✅ Restore interrupt flag: Thread.currentThread().interrupt()");
        System.out.println("3. ✅ Check interrupts in loops: while (!Thread.interrupted())");
        System.out.println("4. ✅ Don't swallow InterruptedException");
        System.out.println("5. ✅ Clean up resources before exiting");
        System.out.println("6. ✅ Use interrupt for graceful cancellation");
        
        System.out.println("\n=== Interrupt Handling Best Practices ===");
        System.out.println("When catching InterruptedException:");
        System.out.println("  1. Log the interruption");
        System.out.println("  2. Restore interrupt flag: Thread.currentThread().interrupt()");
        System.out.println("  3. Clean up resources");
        System.out.println("  4. Exit gracefully (return or throw)");
        
        System.out.println("\nIn loops:");
        System.out.println("  while (!Thread.currentThread().isInterrupted()) {");
        System.out.println("      // do work");
        System.out.println("  }");
        
        System.out.println("\nAfter blocking operations:");
        System.out.println("  Thread.sleep(100);");
        System.out.println("  if (Thread.currentThread().isInterrupted()) {");
        System.out.println("      // handle interrupt");
        System.out.println("      return;");
        System.out.println("  }");
    }
    
    /**
     * Demonstrates graceful shutdown using ExecutorService.
     */
    private static void demonstrateGracefulShutdown() throws InterruptedException {
        System.out.println("\nCreating service and submitting tasks...");
        
        InventoryService inventoryService = new InventoryService();
        OrderProcessingService service = new OrderProcessingService(inventoryService, 3);
        
        // Submit multiple orders
        for (int i = 1; i <= 10; i++) {
            Order order = new Order("ORD-" + String.format("%03d", i + 20), 
                                  "USER-" + (300 + i), 
                                  "PROD-00" + ((i % 5) + 1), 
                                  1, 
                                  99.99);
            service.processOrder(order);
        }
        
        System.out.println("Tasks submitted. Waiting 500ms, then shutting down...");
        Thread.sleep(500);
        
        // ✅ Graceful shutdown (interrupts running tasks)
        service.shutdown();
        
        System.out.println("✓ Shutdown complete - all tasks handled gracefully");
    }
}

