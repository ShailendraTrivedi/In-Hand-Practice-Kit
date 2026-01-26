package com.e_commerce.service;

import com.e_commerce.model.Product;
import com.e_commerce.model.Order;
import com.e_commerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    // Create order (backed by database)
    public Order createOrder(Long productId, Integer quantity) {
        // Get product (throws exception if not found)
        Product product = productService.getProductById(productId);
        
        // Calculate total amount
        Double totalAmount = product.getPrice() * quantity;
        
        // Create order
        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        
        // Save to database using JDBC
        order = orderRepository.save(order);
        
        return order;
    }
    
    // Get order status from database
    public Order getOrderStatus(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    // Update order status in database
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        orderRepository.updateStatus(orderId, status);
    }
}
