package com.e_commerce.controller;

import com.e_commerce.model.Order;
import com.e_commerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // POST - Create order
    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request.getProductId(), request.getQuantity());
    }
    
    // GET - Get order status
    @GetMapping("/status/{id}")
    public Order getOrderStatus(@PathVariable Long id) {
        Order order = orderService.getOrderStatus(id);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return order;
    }
    
    // Inner class for request body
    public static class OrderRequest {
        private Long productId;
        private Integer quantity;
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}