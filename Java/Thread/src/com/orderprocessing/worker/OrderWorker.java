package com.orderprocessing.worker;

import com.orderprocessing.model.Order;
import com.orderprocessing.queue.OrderQueue;
import com.orderprocessing.service.InventoryService;
import com.orderprocessing.service.PaymentService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * OrderWorker processes orders from the queue.
 * 
 * CONCURRENCY CONCEPT #1: Thread & Runnable
 * 
 * A. Definition:
 *    Thread represents a separate execution path in a program. Runnable
 *    is a functional interface that defines a task to be executed by a thread.
 * 
 * B. Why It Is Needed in a Real Backend:
 *    Backend systems must handle multiple requests concurrently. Using
 *    threads allows the system to process multiple orders simultaneously,
 *    maximizing CPU utilization and reducing response times, which is
 *    critical for high-throughput production systems.
 * 
 * C. Safety Measure Used:
 *    Workers are managed by ExecutorService thread pool, which controls
 *    thread lifecycle and prevents resource exhaustion.
 * 
 * D. Safety Measure Definition:
 *    ExecutorService creates and manages a fixed pool of worker threads,
 *    reusing them for multiple tasks. This prevents creating unlimited
 *    threads (which would exhaust system resources) and provides better
 *    performance than creating new threads for each task. Thread pool
 *    size is tuned based on workload characteristics (CPU vs I/O bound).
 */
public class OrderWorker implements Runnable {
    private final OrderQueue orderQueue;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final OrderProcessor orderProcessor;
    private volatile boolean running = true;
    
    public OrderWorker(OrderQueue orderQueue,
                      InventoryService inventoryService,
                      PaymentService paymentService,
                      OrderProcessor orderProcessor) {
        this.orderQueue = orderQueue;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.orderProcessor = orderProcessor;
    }
    
    /**
     * Main worker loop that processes orders from the queue.
     */
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[Worker] " + threadName + " started");
        
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // CONCURRENCY CONCEPT #7: Interrupts
                // 
                // A. Definition:
                //    Thread interruption is a cooperative mechanism where one
                //    thread signals another to stop what it's doing. The
                //    interrupted thread checks its interrupt status and responds
                //    appropriately.
                // 
                // B. Why It Is Needed in a Real Backend:
                //    During graceful shutdown, we need to stop worker threads
                //    that may be blocked waiting for orders. Interrupts allow
                //    us to wake blocked threads and signal them to stop,
                //    preventing indefinite hangs during shutdown.
                // 
                // C. Safety Measure Used:
                //    Regular checks of Thread.currentThread().isInterrupted()
                //    and handling InterruptedException properly.
                // 
                // D. Safety Measure Definition:
                //    When a thread is interrupted, it sets an interrupt flag
                //    and throws InterruptedException if the thread is blocked.
                //    We check the flag in loops and handle the exception by
                //    cleaning up and exiting gracefully. This ensures threads
                //    can be stopped safely without forcing termination.
                
                Order order = orderQueue.dequeue();
                
                if (order == null) {
                    // Queue is empty and shutdown requested
                    break;
                }
                
                // Process the order
                processOrder(order);
                
            } catch (InterruptedException e) {
                // Thread was interrupted - exit gracefully
                Thread.currentThread().interrupt();
                System.out.println("[Worker] " + threadName + " interrupted, shutting down");
                break;
            } catch (Exception e) {
                System.err.println("[Worker] " + threadName + " error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("[Worker] " + threadName + " stopped");
    }
    
    /**
     * Processes a single order through all stages.
     */
    private void processOrder(Order order) {
        String threadName = Thread.currentThread().getName();
        
        try {
            // Check if order was cancelled
            if (order.isCancelled()) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                System.out.println("[Worker] " + threadName + " - Order cancelled: " + order.getOrderId());
                return;
            }
            
            // Stage 1: Check inventory
            if (!inventoryService.checkInventory(order.getProductId(), order.getQuantity())) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.out.println("[Worker] " + threadName + " - Insufficient inventory: " + order.getOrderId());
                return;
            }
            
            order.setStatus(Order.OrderStatus.INVENTORY_CHECKED);
            
            // Stage 2: Process payment (async with Future)
            order.setStatus(Order.OrderStatus.PAYMENT_PROCESSING);
            Future<PaymentService.PaymentResult> paymentFuture = 
                orderProcessor.submitPayment(order);
            
            // Wait for payment result with timeout
            PaymentService.PaymentResult paymentResult;
            try {
                paymentResult = paymentFuture.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                paymentFuture.cancel(true);
                order.setStatus(Order.OrderStatus.FAILED);
                System.out.println("[Worker] " + threadName + " - Payment timeout: " + order.getOrderId());
                return;
            }
            
            if (!paymentResult.isSuccess()) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.out.println("[Worker] " + threadName + " - Payment failed: " + order.getOrderId());
                return;
            }
            
            order.setStatus(Order.OrderStatus.PAYMENT_COMPLETED);
            
            // Stage 3: Update inventory
            if (!inventoryService.updateInventory(order.getProductId(), order.getQuantity())) {
                // Payment succeeded but inventory update failed - need to refund
                orderProcessor.submitRefund(order);
                order.setStatus(Order.OrderStatus.FAILED);
                System.out.println("[Worker] " + threadName + " - Inventory update failed: " + order.getOrderId());
                return;
            }
            
            // Stage 4: Prepare shipping
            order.setStatus(Order.OrderStatus.SHIPPING_PREPARED);
            Thread.sleep(10); // Simulate shipping preparation
            
            // Stage 5: Complete order
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Worker] " + threadName + " - Order completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus(Order.OrderStatus.FAILED);
            System.out.println("[Worker] " + threadName + " - Processing interrupted: " + order.getOrderId());
        } catch (Exception e) {
            order.setStatus(Order.OrderStatus.FAILED);
            System.err.println("[Worker] " + threadName + " - Error processing order: " + order.getOrderId());
            e.printStackTrace();
        }
    }
    
    /**
     * Stops the worker thread gracefully.
     */
    public void stop() {
        running = false;
    }
}

