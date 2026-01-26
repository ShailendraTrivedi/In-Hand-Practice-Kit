package com.e_commerce.service;

import com.e_commerce.dto.response.PaginationResponse;
import com.e_commerce.entity.Order;

public interface IOrderService {
    Order createOrder(Long productId, Integer quantity, String idempotencyKey);
    Order getOrderStatus(Long orderId);
    PaginationResponse<Order> getAllOrders(int page, int size, String sortBy, String direction);
}

