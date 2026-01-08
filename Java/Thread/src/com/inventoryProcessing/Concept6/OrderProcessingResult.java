package com.inventoryProcessing.Concept6;

/**
 * Result of order processing.
 */
public class OrderProcessingResult {
    private final Order order;
    private final boolean success;
    private final String errorMessage;
    private final PaymentResult paymentResult;
    
    private OrderProcessingResult(Order order, boolean success, String errorMessage, PaymentResult paymentResult) {
        this.order = order;
        this.success = success;
        this.errorMessage = errorMessage;
        this.paymentResult = paymentResult;
    }
    
    public static OrderProcessingResult success(Order order, PaymentResult paymentResult) {
        return new OrderProcessingResult(order, true, null, paymentResult);
    }
    
    public static OrderProcessingResult failure(Order order, String errorMessage) {
        return new OrderProcessingResult(order, false, errorMessage, null);
    }
    
    public Order getOrder() {
        return order;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public PaymentResult getPaymentResult() {
        return paymentResult;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("OrderProcessingResult[SUCCESS, Order:%s, Payment:%s]",
                    order.getOrderId(), paymentResult);
        } else {
            return String.format("OrderProcessingResult[FAILED, Order:%s, Error:%s]",
                    order.getOrderId(), errorMessage);
        }
    }
}