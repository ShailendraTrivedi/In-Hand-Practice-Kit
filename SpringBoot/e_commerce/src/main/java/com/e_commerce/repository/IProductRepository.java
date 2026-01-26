package com.e_commerce.repository;

import com.e_commerce.entity.Product;

import java.util.List;

public interface IProductRepository {
    Product save(Product product);
    List<Product> findAll(int page, int size, String sortBy, String direction);
    long count();
    Product findById(Long id);
    boolean deleteById(Long id);
    boolean existsByNameAndPrice(String name, Double price);
}

