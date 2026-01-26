package com.e_commerce.controller;

import com.e_commerce.dto.response.PaginationResponse;
import com.e_commerce.entity.DigitalProduct;
import com.e_commerce.entity.PhysicalProduct;
import com.e_commerce.entity.Product;
import com.e_commerce.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final IProductService productService;
    
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
