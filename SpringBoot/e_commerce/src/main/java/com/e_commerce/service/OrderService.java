package com.e_commerce.service;

import com.e_commerce.dto.PaginationResponse;
import com.e_commerce.model.Product;
import com.e_commerce.model.Order;
import com.e_commerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderRepository orderRepository;

    // Create order (backed by database) - Transactional for data consistency
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long productId, Integer quantity, String idempotencyKey) {
        // Check for existing order with same idempotency key (idempotency check)
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            Order existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOrder != null) {
                // Return existing order - idempotent response
                return existingOrder;
            }
        }
        
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
        order.setIdempotencyKey(idempotencyKey);
        
        // Save to database using JDBC
        // If this fails, entire transaction will rollback
        order = orderRepository.save(order);
        
        return order;
    }

    // Get order status from database
    public Order getOrderStatus(Long orderId) {
        return orderRepository.findById(orderId);
    }

    // Get all orders from database with pagination
    public PaginationResponse<Order> getAllOrders(int page, int size, String sortBy, String direction) {
        List<Order> orders = orderRepository.findAll(page, size, sortBy, direction);
        long totalElements = orderRepository.count();
        return new PaginationResponse<>(orders, page, size, totalElements);
    }
}
