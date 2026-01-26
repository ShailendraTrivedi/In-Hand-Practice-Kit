package com.e_commerce.exception;

public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(String message) {
        super(message);
    }
    
    public OrderNotFoundException(Long id) {
        super("Order with id " + id + " not found");
    }
}