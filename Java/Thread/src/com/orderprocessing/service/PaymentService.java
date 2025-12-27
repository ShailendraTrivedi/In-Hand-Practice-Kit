package com.orderprocessing.service;

import com.orderprocessing.model.Order;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * PaymentService processes payments asynchronously.
 * 
 * CONCURRENCY CONCEPT #6: Callable & Future
 * 
 * A. Definition:
 *    Callable is a functional interface that returns a result and can
 *    throw checked exceptions, unlike Runnable. Future represents the
 *    result of an asynchronous computation.
 * 
 * B. Why It Is Needed in a Real Backend:
 *    Payment processing is I/O-intensive and can take time. Using
 *    Callable/Future allows the system to submit payment tasks and
 *    continue processing other orders while waiting for payment results,
 *    significantly improving throughput and responsiveness.
 * 
 * C. Safety Measure Used:
 *    Each payment is processed as a separate Callable task, and the
 *    result is obtained via Future.get() with timeout to prevent
 *    indefinite blocking.
 * 
 * D. Safety Measure Definition:
 *    Callable tasks are submitted to ExecutorService, which returns
 *    a Future. The Future.get() method blocks until the result is
 *    available, but with timeout prevents deadlock if payment service
 *    hangs. This ensures we can handle both success and failure cases
 *    without blocking the entire system.
 */
public class PaymentService {
    
    /**
     * Processes payment for an order.
     * Simulates I/O operation (network call to payment gateway).
     */
    public Callable<PaymentResult> processPayment(Order order) {
        return () -> {
            // Simulate network latency (50-200ms)
            int delay = ThreadLocalRandom.current().nextInt(50, 200);
            Thread.sleep(delay);
            
            // Simulate payment success/failure (95% success rate)
            boolean success = ThreadLocalRandom.current().nextDouble() < 0.95;
            
            if (success) {
                return new PaymentResult(true, "Payment processed successfully", order.getOrderId());
            } else {
                return new PaymentResult(false, "Payment gateway declined transaction", order.getOrderId());
            }
        };
    }
    
    /**
     * Refunds payment when order is cancelled.
     */
    public Callable<PaymentResult> refundPayment(Order order) {
        return () -> {
            // Simulate refund processing time
            Thread.sleep(ThreadLocalRandom.current().nextInt(30, 100));
            return new PaymentResult(true, "Refund processed successfully", order.getOrderId());
        };
    }
    
    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final String orderId;
        
        public PaymentResult(boolean success, String message, String orderId) {
            this.success = success;
            this.message = message;
            this.orderId = orderId;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getOrderId() {
            return orderId;
        }
    }
}

