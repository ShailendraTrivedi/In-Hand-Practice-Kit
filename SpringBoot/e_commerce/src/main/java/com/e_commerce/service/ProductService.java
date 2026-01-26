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

    // Create product - prevent duplicates using database
    public Product createProduct(Product product) {
        // Check if product already exists (using database)
        if (productRepository.existsByNameAndPrice(product.getName(), product.getPrice())) {
            return null; // Duplicate product
        }

        // Save to database using JDBC
        return productRepository.save(product);
    }

    // Get all products from database with pagination
    public PaginationResponse<Product> getAllProducts(int page, int size, String sortBy, String direction) {
        List<Product> products = productRepository.findAll(page, size, sortBy, direction);
        long totalElements = productRepository.count();
        return new PaginationResponse<>(products, page, size, totalElements);
    }

    // Get product by ID from database - throws exception if not found
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    // Delete product from database
    public boolean deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
}