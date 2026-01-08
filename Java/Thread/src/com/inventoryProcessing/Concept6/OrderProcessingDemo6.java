package com.inventoryProcessing.Concept6;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * CONCEPT #6 DEMONSTRATION: Callable & Future (Payment Result Handling)
 * 
 * This demo shows:
 * 1. BEFORE: Runnable (no return value, callback pattern)
 * 2. AFTER: Callable & Future (clean return values, exception handling)
 * 
 * Interview Tip:
 * - Callable returns value, Runnable doesn't
 * - Callable can throw checked exceptions
 * - Future.get() blocks until result available
 * - Use Future.get(timeout) to avoid indefinite blocking
 * - ExecutionException wraps exceptions from Callable
 */
public class OrderProcessingDemo6 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #6: Callable & Future (Payment Result Handling) ===\n");
        
        // ============================================
        // PART 1: Demonstrating Runnable limitation
        // ============================================
        System.out.println("--- PART 1: Runnable Limitation (No Return Value) ---\n");
        demonstrateRunnableLimitation();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Solution with Callable & Future
        // ============================================
        System.out.println("\n\n--- PART 2: Callable & Future Solution ---\n");
        demonstrateCallableFuture();
    }
    
    /**
     * Demonstrates limitation of Runnable (no return value).
     */
    private static void demonstrateRunnableLimitation() {
        System.out.println("Processing payment with Runnable (callback pattern)...");
        System.out.println("⚠️  Problem: Cannot return payment result directly\n");
        
        PaymentServiceRunnable paymentService = new PaymentServiceRunnable();
        
        // ⚠️  Must use callback - no direct return value
        paymentService.processPayment("ORD-001", 99.99, new PaymentServiceRunnable.PaymentCallback() {
            @Override
            public void onSuccess(String transactionId) {
                System.out.println("[Callback] Payment successful: " + transactionId);
            }
            
            @Override
            public void onFailure(String error) {
                System.out.println("[Callback] Payment failed: " + error);
            }
        });
        
        // Wait for callback
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n--- Problems with Runnable ---");
        System.out.println("❌ No return value");
        System.out.println("❌ Must use callbacks (complex)");
        System.out.println("❌ Difficult to handle exceptions");
        System.out.println("❌ Cannot easily wait for result");
        System.out.println("❌ No timeout support");
    }
    
    /**
     * Demonstrates Callable & Future solution.
     */
    private static void demonstrateCallableFuture() throws InterruptedException {
        System.out.println("Processing orders with Callable & Future...");
        System.out.println("✅ Clean return values, exception handling, timeout support\n");
        
        InventoryService inventoryService = new InventoryService();
        PaymentService paymentService = new PaymentService();
        OrderProcessingService service = new OrderProcessingService(
            inventoryService, paymentService, 5, 10
        );
        
        // Create orders
        List<Order> orders = createOrders(10);
        List<Future<OrderProcessingResult>> futures = new ArrayList<>();
        
        // Submit orders and collect Futures
        System.out.println("Submitting orders...");
        for (Order order : orders) {
            Future<OrderProcessingResult> future = service.processOrderAsync(order);
            futures.add(future);
        }
        
        System.out.println("\nAll orders submitted. Processing asynchronously...\n");
        
        // Process results as they become available
        int completed = 0;
        int successful = 0;
        int failed = 0;
        
        for (int i = 0; i < futures.size(); i++) {
            Future<OrderProcessingResult> future = futures.get(i);
            Order order = orders.get(i);
            
            try {
                // ✅ Get result (blocks until available, with timeout)
                OrderProcessingResult result = future.get(10, TimeUnit.SECONDS);
                completed++;
                
                if (result.isSuccess()) {
                    successful++;
                    System.out.println("✓ Order " + order.getOrderId() + " completed: " + 
                                     result.getPaymentResult().getTransactionId());
                } else {
                    failed++;
                    System.out.println("✗ Order " + order.getOrderId() + " failed: " + 
                                     result.getErrorMessage());
                }
                
            } catch (TimeoutException e) {
                System.out.println("✗ Order " + order.getOrderId() + " timed out");
                future.cancel(true);
                failed++;
            } catch (ExecutionException e) {
                System.out.println("✗ Order " + order.getOrderId() + " exception: " + 
                                 e.getCause().getMessage());
                failed++;
            } catch (InterruptedException e) {
                System.out.println("✗ Interrupted while waiting for order " + order.getOrderId());
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Print statistics
        System.out.println("\n=== Final Statistics ===");
        System.out.println("Total orders: " + orders.size());
        System.out.println("Completed: " + completed);
        System.out.println("Successful: " + successful);
        System.out.println("Failed: " + failed);
        
        // Shutdown
        service.shutdown();
        
        System.out.println("\n=== Key Benefits of Callable & Future ===");
        System.out.println("1. ✅ Return values: Get results directly");
        System.out.println("2. ✅ Exception handling: ExecutionException wraps task exceptions");
        System.out.println("3. ✅ Timeout support: Future.get(timeout) prevents indefinite blocking");
        System.out.println("4. ✅ Cancellation: future.cancel() to stop tasks");
        System.out.println("5. ✅ Non-blocking: Submit tasks, get results later");
        
        System.out.println("\n=== Callable vs Runnable ===");
        System.out.println("Runnable:");
        System.out.println("  - run() returns void");
        System.out.println("  - Cannot throw checked exceptions");
        System.out.println("  - No return value");
        System.out.println("  - Use for fire-and-forget tasks");
        
        System.out.println("\nCallable:");
        System.out.println("  - call() returns value");
        System.out.println("  - Can throw checked exceptions");
        System.out.println("  - Returns Future<V>");
        System.out.println("  - Use when you need results");
        
        System.out.println("\n=== Future Methods ===");
        System.out.println("future.get(): Blocks until result available");
        System.out.println("future.get(timeout): Blocks with timeout");
        System.out.println("future.isDone(): Check if task completed");
        System.out.println("future.cancel(): Cancel the task");
        System.out.println("future.isCancelled(): Check if cancelled");
    }
    
    /**
     * Create sample orders.
     */
    private static List<Order> createOrders(int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String orderId = "ORD-" + String.format("%03d", i);
            String userId = "USER-" + (100 + i);
            String productId = "PROD-00" + ((i % 5) + 1);
            int quantity = (i % 3) + 1;
            double price = 50.0 + (i * 10.0);
            
            orders.add(new Order(orderId, userId, productId, quantity, price));
        }
        return orders;
    }
}