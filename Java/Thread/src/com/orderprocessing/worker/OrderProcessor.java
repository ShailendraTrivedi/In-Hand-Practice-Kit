package com.orderprocessing.worker;

import com.orderprocessing.model.Order;
import com.orderprocessing.service.PaymentService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * OrderProcessor manages payment processing tasks.
 * Uses ExecutorService to handle async payment operations.
 */
public class OrderProcessor {
    private final ExecutorService paymentExecutor;
    private final PaymentService paymentService;
    
    public OrderProcessor(ExecutorService paymentExecutor, PaymentService paymentService) {
        this.paymentExecutor = paymentExecutor;
        this.paymentService = paymentService;
    }
    
    public Future<PaymentService.PaymentResult> submitPayment(Order order) {
        return paymentExecutor.submit(paymentService.processPayment(order));
    }
    
    public Future<PaymentService.PaymentResult> submitRefund(Order order) {
        return paymentExecutor.submit(paymentService.refundPayment(order));
    }
}

