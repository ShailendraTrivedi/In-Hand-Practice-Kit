package com.e_commerce.service;

import com.e_commerce.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    @Autowired
    private ProductService productService;
    
    // Functional interface for price threshold
    private static final Predicate<Product> EXPENSIVE_PRODUCT_FILTER = 
        product -> product.getPrice() != null && product.getPrice() > 100.0;
    
    // Functional interface for extracting price
    private static final Function<Product, Double> PRICE_EXTRACTOR = 
        Product::getPrice;
    
    // GET total revenue using Stream API with reduce
    public Double getTotalRevenue() {
        return productService.getAllProducts()
                .stream()
                .filter(product -> product.getPrice() != null)
                .map(Product::getPrice)
                .reduce(0.0, Double::sum);
    }
    
    // GET expensive products using Stream API with filter and custom collector
    public List<Product> getExpensiveProducts() {
        return productService.getAllProducts()
                .stream()
                .filter(EXPENSIVE_PRODUCT_FILTER)
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .collect(Collectors.toList());
    }
    
    // Demonstrate parallel streams
    public Double getTotalRevenueParallel() {
        return productService.getAllProducts()
                .parallelStream()
                .filter(product -> product.getPrice() != null)
                .mapToDouble(Product::getPrice)
                .sum();
    }
    
    // Custom collector example - grouping products by type
    public Map<String, List<Product>> getProductsByType() {
        return productService.getAllProducts()
                .stream()
                .collect(Collectors.groupingBy(Product::getProductType));
    }
    
    // Custom collector - average price
    public Double getAveragePrice() {
        return productService.getAllProducts()
                .stream()
                .filter(product -> product.getPrice() != null)
                .collect(Collectors.averagingDouble(Product::getPrice));
    }
    
    // Lambda with method reference
    public List<String> getAllProductNames() {
        return productService.getAllProducts()
                .stream()
                .map(Product::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    // Parallel processing using Fork/Join framework
    public Map<String, Object> generateParallelReport() {
        List<Product> products = productService.getAllProducts();
        
        // ForkJoinTask for parallel computation
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        
        // RecursiveTask for calculating total revenue in parallel
        RecursiveTask<Double> revenueTask = new RevenueCalculationTask(products, 0, products.size());
        Double totalRevenue = forkJoinPool.invoke(revenueTask);
        
        // RecursiveTask for counting expensive products
        RecursiveTask<Integer> countTask = new ExpensiveProductCountTask(products, 0, products.size());
        Integer expensiveCount = forkJoinPool.invoke(countTask);
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("expensiveProductCount", expensiveCount);
        report.put("totalProducts", products.size());
        report.put("averagePrice", totalRevenue / products.size());
        
        return report;
    }
    
    // RecursiveTask for calculating revenue using Fork/Join
    private static class RevenueCalculationTask extends RecursiveTask<Double> {
        private static final int THRESHOLD = 10;
        private final List<Product> products;
        private final int start;
        private final int end;
        
        public RevenueCalculationTask(List<Product> products, int start, int end) {
            this.products = products;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Double compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                // Base case: compute directly
                double sum = 0.0;
                for (int i = start; i < end; i++) {
                    if (products.get(i).getPrice() != null) {
                        sum += products.get(i).getPrice();
                    }
                }
                return sum;
            } else {
                // Split task
                int mid = start + length / 2;
                RevenueCalculationTask left = new RevenueCalculationTask(products, start, mid);
                RevenueCalculationTask right = new RevenueCalculationTask(products, mid, end);
                
                // Fork and join
                left.fork();
                Double rightResult = right.compute();
                Double leftResult = left.join();
                
                return leftResult + rightResult;
            }
        }
    }
    
    // RecursiveTask for counting expensive products using Fork/Join
    private static class ExpensiveProductCountTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 10;
        private final List<Product> products;
        private final int start;
        private final int end;
        
        public ExpensiveProductCountTask(List<Product> products, int start, int end) {
            this.products = products;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Integer compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                // Base case: count directly
                int count = 0;
                for (int i = start; i < end; i++) {
                    if (products.get(i).getPrice() != null && products.get(i).getPrice() > 100.0) {
                        count++;
                    }
                }
                return count;
            } else {
                // Split task
                int mid = start + length / 2;
                ExpensiveProductCountTask left = new ExpensiveProductCountTask(products, start, mid);
                ExpensiveProductCountTask right = new ExpensiveProductCountTask(products, mid, end);
                
                // Fork and join
                left.fork();
                Integer rightResult = right.compute();
                Integer leftResult = left.join();
                
                return leftResult + rightResult;
            }
        }
    }
}