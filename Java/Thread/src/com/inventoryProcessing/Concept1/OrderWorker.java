package com.inventoryProcessing.Concept1;

/**
 * CONCEPT #1: Thread & Runnable
 * 
 * This class demonstrates the fundamental building block of Java concurrency:
 * - Implements Runnable interface to define a task
 * - The task will be executed by a Thread
 * 
 * Definition:
 * - Runnable: A functional interface that represents a task to be executed
 * - Thread: A Java class that executes a Runnable in a separate thread
 * 
 * Why needed in e-commerce:
 * - Process multiple orders concurrently instead of sequentially
 * - Improve system throughput and user experience
 * - Handle multiple customer requests simultaneously
 * 
 * Safety Measure (for Concept #1):
 * - Thread creation and management: We create threads explicitly
 * - Using join() to wait for thread completion
 * - NOTE: We're NOT handling shared state yet (that comes in Concept #2)
 */
public class OrderWorker implements Runnable {
    
    private final Order order;
    private final int workerId;
    
    /**
     * Constructor for OrderWorker.
     * Each worker processes a single order.
     * 
     * @param order The order to process
     * @param workerId Unique identifier for this worker (for logging)
     */
    public OrderWorker(Order order, int workerId) {
        this.order = order;
        this.workerId = workerId;
    }
    
    /**
     * The run() method is required by Runnable interface.
     * This is the task that will be executed in a separate thread.
     * 
     * This method simulates order processing:
     * 1. Check inventory
     * 2. Process payment
     * 3. Prepare shipping
     * 4. Complete order
     */
    @Override
    public void run() {
        try {
            // Set thread name for debugging (good practice)
            Thread.currentThread().setName("OrderWorker-" + workerId);
            
            System.out.println("[Worker-" + workerId + "] Starting processing: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // Simulate order processing steps
            // Step 1: Check inventory (simulate I/O delay)
            System.out.println("[Worker-" + workerId + "] Checking inventory for: " + order.getProductId());
            Thread.sleep(100); // Simulate network/database call
            
            // Step 2: Process payment (simulate I/O delay)
            System.out.println("[Worker-" + workerId + "] Processing payment: $" + order.getPrice());
            Thread.sleep(150); // Simulate payment gateway call
            
            // Step 3: Prepare shipping
            System.out.println("[Worker-" + workerId + "] Preparing shipping for: " + order.getOrderId());
            Thread.sleep(50); // Simulate shipping preparation
            
            // Step 4: Complete order
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            // Thread was interrupted (we'll cover interrupts in Concept #7)
            System.out.println("[Worker-" + workerId + "] ✗ Interrupted while processing: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.FAILED);
            Thread.currentThread().interrupt(); // Restore interrupt flag
        } catch (Exception e) {
            // Handle any other exceptions
            System.out.println("[Worker-" + workerId + "] ✗ Error processing: " + order.getOrderId() + " - " + e.getMessage());
            order.setStatus(Order.OrderStatus.FAILED);
        }
    }
    
    /**
     * Getter for the order being processed.
     * Useful for checking order status after thread completes.
     */
    public Order getOrder() {
        return order;
    }
}