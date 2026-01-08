package com.inventoryProcessing.Concept6;

/**
 * Result of payment processing.
 * This is what Callable returns from payment processing.
 */
public class PaymentResult {
    private final String transactionId;
    private final boolean success;
    private final String errorMessage;
    private final double amount;
    private final long processingTimeMs;
    
    public PaymentResult(String transactionId, boolean success, String errorMessage, 
                       double amount, long processingTimeMs) {
        this.transactionId = transactionId;
        this.success = success;
        this.errorMessage = errorMessage;
        this.amount = amount;
        this.processingTimeMs = processingTimeMs;
    }
    
    public static PaymentResult success(String transactionId, double amount, long processingTimeMs) {
        return new PaymentResult(transactionId, true, null, amount, processingTimeMs);
    }
    
    public static PaymentResult failure(String errorMessage, double amount, long processingTimeMs) {
        return new PaymentResult(null, false, errorMessage, amount, processingTimeMs);
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("PaymentResult[SUCCESS, TransactionId:%s, Amount:%.2f, Time:%dms]",
                    transactionId, amount, processingTimeMs);
        } else {
            return String.format("PaymentResult[FAILED, Error:%s, Amount:%.2f, Time:%dms]",
                    errorMessage, amount, processingTimeMs);
        }
    }
}