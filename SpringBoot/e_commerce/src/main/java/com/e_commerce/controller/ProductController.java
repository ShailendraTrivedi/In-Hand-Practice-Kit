package com.e_commerce.controller;

import com.e_commerce.dto.PaginationResponse;
import com.e_commerce.model.DigitalProduct;
import com.e_commerce.model.PhysicalProduct;
import com.e_commerce.model.Product;
import com.e_commerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public PaginationResponse<Product> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return productService.getAllProducts(page, size, sortBy, direction);
    }
    
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
    
    @PostMapping("/physical")
    public PhysicalProduct createPhysicalProduct(@RequestBody PhysicalProduct product) {
        return (PhysicalProduct) productService.createProduct(product);
    }
    
    @PostMapping("/digital")
    public DigitalProduct createDigitalProduct(@RequestBody DigitalProduct product) {
        return (DigitalProduct) productService.createProduct(product);
    }
    
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return "Product deleted successfully";
        }
        return "Product not found";
    }
}