package com.inventoryProcessing.Concept9;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * CONCEPT #9 - AFTER: OrderProcessingService WITH Deadlock Prevention
 * 
 * ✅ THIS VERSION PREVENTS DEADLOCK ✅
 * 
 * Definition:
 * - Deadlock: Situation where two or more threads are blocked forever
 * - Lock Ordering: Always acquire locks in the same order
 * - Timeout-based locks: Use tryLock() with timeout to avoid indefinite blocking
 * - Design patterns: Avoid nested locks when possible
 * 
 * Why needed in e-commerce:
 * - Multiple resources: Inventory, payment, shipping need coordination
 * - High concurrency: Many orders processed simultaneously
 * - System reliability: Deadlocks can freeze the entire system
 * - User experience: Deadlocks cause orders to hang indefinitely
 * 
 * Safety Measure:
 * - Lock Ordering: Always acquire locks in consistent order
 * - Timeout-based locks: Use tryLock(timeout) to detect deadlocks
 * - Lock-free design: Use thread-safe collections when possible
 * - Minimize lock scope: Hold locks for shortest time possible
 * 
 * Interview Tip:
 * - Always acquire locks in the same order (alphabetical, by ID, etc.)
 * - Use tryLock() with timeout to detect potential deadlocks
 * - Consider using lock-free data structures (ConcurrentHashMap)
 * - Minimize number of locks and lock scope
 * - Use lock ordering even if it seems less efficient
 */
public class OrderProcessingService {
    
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    
    // ✅ Use ReentrantLock for timeout-based locking
    private final Lock inventoryLock = new ReentrantLock();
    private final Lock paymentLock = new ReentrantLock();
    
    // ✅ Lock ordering: Always acquire locks in this order: inventory, then payment
    // This prevents circular wait condition (deadlock)
    
    public OrderProcessingService(InventoryService inventoryService, PaymentService paymentService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
    }
    
    /**
     * Process order with lock ordering.
     * ✅ SOLUTION 1: Always acquire locks in the same order
     * 
     * Lock Ordering Strategy:
     * 1. Always acquire inventory lock first
     * 2. Then acquire payment lock
     * 3. This prevents circular wait condition
     */
    public void processOrderWithLockOrdering(Order order) {
        System.out.println("[Service] Processing with lock ordering: " + order.getOrderId());
        
        // ✅ SOLUTION 1: Lock Ordering
        // Always acquire locks in the same order (inventory, then payment)
        // This prevents circular wait condition
        
        synchronized (inventoryService.getLock()) {
            System.out.println("[Service] Acquired inventory lock: " + order.getOrderId());
            
            boolean reserved = inventoryService.reserveStockWithLock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Service] Insufficient stock: " + order.getOrderId());
                return;
            }
            
            // ✅ Same order: inventory first, then payment
            synchronized (paymentService.getLock()) {
                System.out.println("[Service] Acquired payment lock: " + order.getOrderId());
                
                boolean paid = paymentService.processPaymentWithLock(order.getOrderId(), order.getPrice());
                if (paid) {
                    order.setStatus(Order.OrderStatus.COMPLETED);
                    System.out.println("[Service] ✓ Completed: " + order.getOrderId());
                } else {
                    order.setStatus(Order.OrderStatus.FAILED);
                }
            }
        }
    }
    
    /**
     * Process order with timeout-based locks.
     * ✅ SOLUTION 2: Use tryLock() with timeout to detect deadlocks
     * 
     * Benefits:
     * - Detects potential deadlocks (timeout)
     * - Allows retry or alternative path
     * - Prevents indefinite blocking
     */
    public void processOrderWithTimeout(Order order, long timeoutMs) {
        System.out.println("[Service] Processing with timeout: " + order.getOrderId());
        
        boolean inventoryLocked = false;
        boolean paymentLocked = false;
        
        try {
            // ✅ Try to acquire inventory lock with timeout
            if (inventoryLock.tryLock(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                inventoryLocked = true;
                System.out.println("[Service] Acquired inventory lock: " + order.getOrderId());
                
                boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
                if (!reserved) {
                    System.out.println("[Service] Insufficient stock: " + order.getOrderId());
                    return;
                }
                
                // ✅ Try to acquire payment lock with timeout
                if (paymentLock.tryLock(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                    paymentLocked = true;
                    System.out.println("[Service] Acquired payment lock: " + order.getOrderId());
                    
                    boolean paid = paymentService.processPayment(order.getOrderId(), order.getPrice());
                    if (paid) {
                        order.setStatus(Order.OrderStatus.COMPLETED);
                        System.out.println("[Service] ✓ Completed: " + order.getOrderId());
                    } else {
                        order.setStatus(Order.OrderStatus.FAILED);
                    }
                } else {
                    System.out.println("[Service] ✗ Could not acquire payment lock (timeout): " + order.getOrderId());
                    order.setStatus(Order.OrderStatus.FAILED);
                }
            } else {
                System.out.println("[Service] ✗ Could not acquire inventory lock (timeout): " + order.getOrderId());
                order.setStatus(Order.OrderStatus.FAILED);
            }
        } catch (InterruptedException e) {
            System.out.println("[Service] ✗ Interrupted: " + order.getOrderId());
            Thread.currentThread().interrupt();
            order.setStatus(Order.OrderStatus.FAILED);
        } finally {
            // ✅ Always release locks in reverse order
            if (paymentLocked) {
                paymentLock.unlock();
            }
            if (inventoryLocked) {
                inventoryLock.unlock();
            }
        }
    }
    
    /**
     * Process order with lock-free design.
     * ✅ SOLUTION 3: Use thread-safe collections (no explicit locks)
     * 
     * Best solution: Avoid locks entirely when possible
     */
    public void processOrderLockFree(Order order) {
        System.out.println("[Service] Processing lock-free: " + order.getOrderId());
        order.setStatus(Order.OrderStatus.PROCESSING);
        
        // ✅ Use thread-safe collections (ConcurrentHashMap)
        // No explicit locks needed - atomic operations
        boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
        if (!reserved) {
            System.out.println("[Service] Insufficient stock: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.FAILED);
            return;
        }
        
        // ✅ Payment processing (no locks needed if payment service is stateless)
        boolean paid = paymentService.processPayment(order.getOrderId(), order.getPrice());
        if (paid) {
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Service] ✓ Completed: " + order.getOrderId());
        } else {
            order.setStatus(Order.OrderStatus.FAILED);
        }
    }
}

