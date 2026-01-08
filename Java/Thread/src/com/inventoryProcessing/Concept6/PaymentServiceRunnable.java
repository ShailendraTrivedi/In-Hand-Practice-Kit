package com.inventoryProcessing.Concept6;

import java.util.concurrent.ThreadLocalRandom;

/**
 * CONCEPT #6 - BEFORE: PaymentService WITHOUT Callable/Future
 * 
 * ⚠️  THIS VERSION CANNOT RETURN RESULTS ⚠️
 * 
 * Problem:
 * - Runnable doesn't return values
 * - Cannot get payment result (success/failure, transaction ID)
 * - Must use shared state or callbacks (complex, error-prone)
 * - No way to handle exceptions properly
 * 
 * Issues:
 * - No return value from payment processing
 * - Difficult to know when payment completes
 * - Must poll or use callbacks
 * - Exception handling is complex
 */
public class PaymentServiceRunnable {
    
    /**
     * Process payment using Runnable (no return value).
     * ⚠️  PROBLEM: Cannot return payment result
     */
    public void processPayment(String orderId, double amount, PaymentCallback callback) {
        // Simulate payment processing
        new Thread(() -> {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
                
                // Simulate success/failure
                boolean success = ThreadLocalRandom.current().nextDouble() > 0.1; // 90% success
                
                if (success) {
                    String transactionId = "TXN-" + System.currentTimeMillis();
                    // ⚠️  Must use callback - no direct return value
                    callback.onSuccess(transactionId);
                } else {
                    // ⚠️  Must use callback - no direct return value
                    callback.onFailure("Payment gateway error");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                callback.onFailure("Payment processing interrupted");
            }
        }).start();
    }
    
    /**
     * Callback interface (workaround for no return value).
     * ⚠️  COMPLEX: Requires callback pattern
     */
    public interface PaymentCallback {
        void onSuccess(String transactionId);
        void onFailure(String error);
    }
}