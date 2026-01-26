package com.e_commerce.controller;

import com.e_commerce.model.Product;
import com.e_commerce.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    // GET total revenue
    @GetMapping("/total-revenue")
    public Map<String, Double> getTotalRevenue() {
        Double revenue = reportService.getTotalRevenue();
        return Map.of("totalRevenue", revenue);
    }
    
    // GET expensive products
    @GetMapping("/expensive-products")
    public List<Product> getExpensiveProducts() {
        return reportService.getExpensiveProducts();
    }
    
    // GET parallel report using Fork/Join
    @GetMapping("/parallel")
    public Map<String, Object> getParallelReport() {
        return reportService.generateParallelReport();
    }
}