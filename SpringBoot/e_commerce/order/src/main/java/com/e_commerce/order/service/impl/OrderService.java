package com.e_commerce.order.service.impl;

import com.e_commerce.order.dto.ProductDto;
import com.e_commerce.order.entity.Order;
import com.e_commerce.order.dto.response.PaginationResponse;
import com.e_commerce.order.repository.IOrderRepository;
import com.e_commerce.order.service.IOrderService;
import com.e_commerce.order.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final IProductService productService;
    private final IOrderRepository orderRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long productId, Integer quantity, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            Order existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOrder != null) {
                return existingOrder;
            }
        }

        ProductDto product = productService.getProductById(productId);
        Double totalAmount = product.getPrice() * quantity;

        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setIdempotencyKey(idempotencyKey);

        order = orderRepository.save(order);
        return order;
    }

    @Override
    public Order getOrderStatus(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public PaginationResponse<Order> getAllOrders(int page, int size, String sortBy, String direction) {
        List<Order> orders = orderRepository.findAll(page, size, sortBy, direction);
        long totalElements = orderRepository.count();
        return new PaginationResponse<>(orders, page, size, totalElements);
    }
}