package com.e_commerce.product.service;

import com.e_commerce.product.dto.response.PaginationResponse;
import com.e_commerce.product.entity.Product;

public interface IProductService {
    Product createProduct(Product product);
    PaginationResponse<Product> getAllProducts(int page, int size, String sortBy, String direction);
    Product getProductById(Long id);
    boolean deleteProduct(Long id);
}

