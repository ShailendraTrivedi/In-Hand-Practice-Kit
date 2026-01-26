package com.e_commerce.controller;

import com.e_commerce.model.DigitalProduct;
import com.e_commerce.model.PhysicalProduct;
import com.e_commerce.model.Product;
import com.e_commerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    // GET all products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    
    // GET product by id - will throw ProductNotFoundException if not found
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
    
    // POST - Create physical product
    @PostMapping("/physical")
    public PhysicalProduct createPhysicalProduct(@RequestBody PhysicalProduct product) {
        return (PhysicalProduct) productService.createProduct(product);
    }
    
    // POST - Create digital product
    @PostMapping("/digital")
    public DigitalProduct createDigitalProduct(@RequestBody DigitalProduct product) {
        return (DigitalProduct) productService.createProduct(product);
    }
    
    // DELETE product by id
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return "Product deleted successfully";
        }
        return "Product not found";
    }
}