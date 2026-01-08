package com.inventoryProcessing.Concept9;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * PaymentService for Concept #9.
 * Self-contained within Concept9 folder.
 */
public class PaymentService {
    
    private final Object paymentLock = new Object(); // For deadlock demonstration
    
    /**
     * Get lock object for deadlock demonstration.
     * ⚠️  Only used in deadlock scenario
     */
    public Object getLock() {
        return paymentLock;
    }
    
    /**
     * Process payment with explicit lock (for deadlock demo).
     * ⚠️  Uses synchronized block for deadlock demonstration
     */
    public boolean processPaymentWithLock(String orderId, double amount) {
        synchronized (paymentLock) {
            System.out.println("[PaymentService] Processing payment for: " + orderId);
            
            try {
                Thread.sleep(10); // Simulate payment processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            
            // Simulate success/failure
            boolean success = ThreadLocalRandom.current().nextDouble() > 0.1;
            
            if (success) {
                String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
                System.out.println("[PaymentService] ✓ Payment successful: " + transactionId);
                return true;
            } else {
                System.out.println("[PaymentService] ✗ Payment failed");
                return false;
            }
        }
    }
    
    /**
     * Process payment (thread-safe, no deadlock risk).
     * ✅ No explicit locking needed
     */
    public boolean processPayment(String orderId, double amount) {
        System.out.println("[PaymentService] Processing payment for: " + orderId);
        
        try {
            Thread.sleep(10); // Simulate payment processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        // Simulate success/failure
        boolean success = ThreadLocalRandom.current().nextDouble() > 0.1;
        
        if (success) {
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("[PaymentService] ✓ Payment successful: " + transactionId);
            return true;
        } else {
            System.out.println("[PaymentService] ✗ Payment failed");
            return false;
        }
    }
}

