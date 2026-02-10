package com.e_commerce.order.service;

import com.e_commerce.order.dto.ProductDto;

public interface IProductService {
    ProductDto getProductById(Long id);
}