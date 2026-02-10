package com.e_commerce.order.repository;

import com.e_commerce.order.entity.Order;

import java.util.List;

public interface IOrderRepository {
    Order save(Order order);
    Order findByIdempotencyKey(String idempotencyKey);
    Order findById(Long id);
    List<Order> findAll(int page, int size, String sortBy, String direction);
    long count();
}

