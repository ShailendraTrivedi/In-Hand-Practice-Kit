package com.inventoryProcessing.Concept6;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CONCEPT #6 - AFTER: PaymentService WITH Callable
 * 
 * ✅ THIS VERSION RETURNS RESULTS ✅
 * 
 * Definition:
 * - Callable<V>: Like Runnable but returns a value and can throw exceptions
 * - call(): Method that returns V (unlike run() which returns void)
 * - Can throw checked exceptions (unlike Runnable)
 * 
 * Why needed in e-commerce:
 * - Payment processing returns results (success/failure, transaction ID)
 * - Need to handle payment exceptions properly
 * - Want to get results asynchronously without blocking
 * - Future.get() provides clean way to retrieve results
 * 
 * Safety Measure:
 * - Callable: Returns value and handles exceptions
 * - Future: Represents asynchronous result
 * - Future.get(): Blocks until result available (with timeout)
 * - ExecutionException: Wraps exceptions thrown by Callable
 */
public class PaymentService {
    
    /**
     * Process payment and return result.
     * ✅ Returns Callable that can be submitted to ExecutorService
     * 
     * @param orderId Order ID
     * @param amount Payment amount
     * @return Callable that returns PaymentResult
     */
    public Callable<PaymentResult> processPayment(String orderId, double amount) {
        return new PaymentCallable(orderId, amount);
    }
    
    /**
     * Callable implementation for payment processing.
     * ✅ Returns PaymentResult (unlike Runnable which returns void)
     * ✅ Can throw checked exceptions
     */
    private static class PaymentCallable implements Callable<PaymentResult> {
        private final String orderId;
        private final double amount;
        
        public PaymentCallable(String orderId, double amount) {
            this.orderId = orderId;
            this.amount = amount;
        }
        
        /**
         * Process payment and return result.
         * ✅ Returns PaymentResult (not void like Runnable)
         * ✅ Can throw checked exceptions
         */
        @Override
        public PaymentResult call() throws Exception {
            long startTime = System.currentTimeMillis();
            
            System.out.println("[PaymentService] Processing payment for order: " + orderId + 
                             ", Amount: $" + amount);
            
            // Simulate payment gateway call
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
            
            // Simulate various payment scenarios
            double random = ThreadLocalRandom.current().nextDouble();
            
            if (random < 0.05) {
                // 5% chance: Insufficient funds
                long processingTime = System.currentTimeMillis() - startTime;
                return PaymentResult.failure("Insufficient funds", amount, processingTime);
            } else if (random < 0.10) {
                // 5% chance: Payment gateway timeout
                long processingTime = System.currentTimeMillis() - startTime;
                return PaymentResult.failure("Payment gateway timeout", amount, processingTime);
            } else if (random < 0.15) {
                // 5% chance: Card declined
                long processingTime = System.currentTimeMillis() - startTime;
                return PaymentResult.failure("Card declined", amount, processingTime);
            } else {
                // 85% chance: Success
                String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                long processingTime = System.currentTimeMillis() - startTime;
                
                System.out.println("[PaymentService] ✓ Payment successful: " + transactionId);
                return PaymentResult.success(transactionId, amount, processingTime);
            }
        }
    }
    
    /**
     * Process payment with timeout simulation.
     * Demonstrates handling slow payment gateways.
     */
    public Callable<PaymentResult> processPaymentWithTimeout(String orderId, double amount, long timeoutMs) {
        return () -> {
            long startTime = System.currentTimeMillis();
            
            System.out.println("[PaymentService] Processing payment (timeout: " + timeoutMs + "ms)");
            
            // Simulate slow payment gateway
            Thread.sleep(timeoutMs + 100); // Exceeds timeout
            
            // This won't be reached if timeout is handled properly
            return PaymentResult.failure("Payment timeout", amount, 
                                       System.currentTimeMillis() - startTime);
        };
    }
}