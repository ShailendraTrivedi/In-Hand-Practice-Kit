package com.e_commerce.service;

import com.e_commerce.dto.PaginationResponse;
import com.e_commerce.exception.ProductNotFoundException;
import com.e_commerce.model.Product;
import com.e_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Product product) {
        if (productRepository.existsByNameAndPrice(product.getName(), product.getPrice())) {
            return null;
        }
        return productRepository.save(product);
    }

    public PaginationResponse<Product> getAllProducts(int page, int size, String sortBy, String direction) {
        List<Product> products = productRepository.findAll(page, size, sortBy, direction);
        long totalElements = productRepository.count();
        return new PaginationResponse<>(products, page, size, totalElements);
    }

    public Product getProductById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public boolean deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
}