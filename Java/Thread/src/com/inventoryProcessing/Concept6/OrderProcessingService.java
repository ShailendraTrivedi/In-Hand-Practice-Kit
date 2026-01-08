package com.inventoryProcessing.Concept6;

import java.util.concurrent.*;

/**
 * CONCEPT #6: OrderProcessingService with Future support
 * 
 * Demonstrates using Callable and Future for payment processing.
 */
public class OrderProcessingService {
    
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ExecutorService executorService;
    private final ExecutorService paymentExecutorService;
    
    /**
     * Create order processing service.
     * 
     * @param inventoryService Inventory service
     * @param paymentService Payment service
     * @param orderPoolSize Thread pool size for order processing
     * @param paymentPoolSize Thread pool size for payment processing
     */
    public OrderProcessingService(InventoryService inventoryService, 
                                  PaymentService paymentService,
                                  int orderPoolSize, 
                                  int paymentPoolSize) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        
        // Thread pool for order processing
        this.executorService = Executors.newFixedThreadPool(orderPoolSize);
        
        // Separate thread pool for payment processing (I/O-bound, can use more threads)
        this.paymentExecutorService = Executors.newFixedThreadPool(paymentPoolSize);
        
        System.out.println("[Service] Created order pool: " + orderPoolSize + 
                         ", payment pool: " + paymentPoolSize);
    }
    
    /**
     * Process order with payment result handling using Future.
     * ✅ Uses Callable and Future to get payment results
     */
    public Future<OrderProcessingResult> processOrderAsync(Order order) {
        // Submit order processing task
        return executorService.submit(() -> {
            return processOrder(order);
        });
    }
    
    /**
     * Process order and return result.
     * ✅ Uses Future to handle payment processing asynchronously
     */
    private OrderProcessingResult processOrder(Order order) {
        try {
            System.out.println("[Service] Processing order: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // Step 1: Check inventory
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Service] ✗ Insufficient stock: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.FAILED);
                return OrderProcessingResult.failure(order, "Insufficient stock");
            }
            
            System.out.println("[Service] ✓ Inventory reserved: " + order.getOrderId());
            
            // Step 2: Process payment using Callable and Future
            // ✅ Submit payment Callable to payment executor
            Future<PaymentResult> paymentFuture = paymentExecutorService.submit(
                paymentService.processPayment(order.getOrderId(), order.getPrice())
            );
            
            // Step 3: Do other work while payment processes (non-blocking)
            System.out.println("[Service] Payment processing started, preparing shipping...");
            Thread.sleep(50); // Simulate shipping preparation
            
            // Step 4: Get payment result (blocks until available)
            // ✅ Future.get() retrieves the result
            PaymentResult paymentResult;
            try {
                // Wait for payment with timeout (5 seconds)
                paymentResult = paymentFuture.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.out.println("[Service] ✗ Payment timeout: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.FAILED);
                paymentFuture.cancel(true); // Cancel the payment task
                return OrderProcessingResult.failure(order, "Payment timeout");
            } catch (ExecutionException e) {
                System.out.println("[Service] ✗ Payment exception: " + order.getOrderId() + 
                                 " - " + e.getCause().getMessage());
                order.setStatus(Order.OrderStatus.FAILED);
                return OrderProcessingResult.failure(order, "Payment exception: " + e.getCause().getMessage());
            }
            
            // Step 5: Handle payment result
            if (!paymentResult.isSuccess()) {
                System.out.println("[Service] ✗ Payment failed: " + order.getOrderId() + 
                                 " - " + paymentResult.getErrorMessage());
                order.setStatus(Order.OrderStatus.FAILED);
                return OrderProcessingResult.failure(order, paymentResult.getErrorMessage());
            }
            
            System.out.println("[Service] ✓ Payment successful: " + order.getOrderId() + 
                             ", Transaction: " + paymentResult.getTransactionId());
            
            // Step 6: Complete order
            order.setStatus(Order.OrderStatus.COMPLETED);
            return OrderProcessingResult.success(order, paymentResult);
            
        } catch (InterruptedException e) {
            System.out.println("[Service] ✗ Interrupted: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.FAILED);
            Thread.currentThread().interrupt();
            return OrderProcessingResult.failure(order, "Processing interrupted");
        } catch (Exception e) {
            System.out.println("[Service] ✗ Error: " + order.getOrderId() + " - " + e.getMessage());
            order.setStatus(Order.OrderStatus.FAILED);
            return OrderProcessingResult.failure(order, e.getMessage());
        }
    }
    
    /**
     * Shutdown services.
     */
    public void shutdown() throws InterruptedException {
        System.out.println("[Service] Shutting down...");
        executorService.shutdown();
        paymentExecutorService.shutdown();
        
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        paymentExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("[Service] ✓ Shutdown complete");
    }
}