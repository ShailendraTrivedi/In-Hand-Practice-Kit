package com.e_commerce.order.service;

import com.e_commerce.order.dto.response.PaginationResponse;
import com.e_commerce.order.entity.Order;

public interface IOrderService {
    Order createOrder(Long productId, Integer quantity, String idempotencyKey);
    Order getOrderStatus(Long orderId);
    PaginationResponse<Order> getAllOrders(int page, int size, String sortBy, String direction);
}

