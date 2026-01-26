package com.e_commerce.controller;

import com.e_commerce.dto.response.PaginationResponse;
import com.e_commerce.entity.Order;
import com.e_commerce.exception.OrderNotFoundException;
import com.e_commerce.service.IOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final IOrderService orderService;
    
    @GetMapping
    public PaginationResponse<Order> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return orderService.getAllOrders(page, size, sortBy, direction);
    }
    
    @PostMapping
    public Order createOrder(
            @RequestBody OrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return orderService.createOrder(request.getProductId(), request.getQuantity(), idempotencyKey);
    }
    
    @GetMapping("/status/{id}")
    public Order getOrderStatus(@PathVariable Long id) {
        Order order = orderService.getOrderStatus(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        return order;
    }
    
    @Data
    public static class OrderRequest {
        private Long productId;
        private Integer quantity;
    }
}
