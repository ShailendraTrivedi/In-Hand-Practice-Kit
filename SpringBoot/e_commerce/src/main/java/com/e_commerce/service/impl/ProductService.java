package com.e_commerce.service.impl;

import com.e_commerce.dto.response.PaginationResponse;
import com.e_commerce.entity.Product;
import com.e_commerce.exception.ProductNotFoundException;
import com.e_commerce.repository.IProductRepository;
import com.e_commerce.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final IProductRepository productRepository;

    @Override
    public Product createProduct(Product product) {
        if (productRepository.existsByNameAndPrice(product.getName(), product.getPrice())) {
            return null;
        }
        return productRepository.save(product);
    }

    @Override
    public PaginationResponse<Product> getAllProducts(int page, int size, String sortBy, String direction) {
        List<Product> products = productRepository.findAll(page, size, sortBy, direction);
        long totalElements = productRepository.count();
        return new PaginationResponse<>(products, page, size, totalElements);
    }

    @Override
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    @Override
    public boolean deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
}

