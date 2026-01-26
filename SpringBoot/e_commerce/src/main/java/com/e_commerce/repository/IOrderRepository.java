package com.e_commerce.repository;

import com.e_commerce.entity.Order;

import java.util.List;

public interface IOrderRepository {
    Order save(Order order);
    Order findByIdempotencyKey(String idempotencyKey);
    Order findById(Long id);
    List<Order> findAll(int page, int size, String sortBy, String direction);
    long count();
}

