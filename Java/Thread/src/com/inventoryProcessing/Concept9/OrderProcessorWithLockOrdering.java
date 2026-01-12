package com.inventoryProcessing.Concept9;

/**
 * CONCEPT #9 - AFTER: OrderProcessor WITH Lock Ordering
 * 
 * ✅ THIS VERSION PREVENTS DEADLOCK ✅
 * 
 * Definition:
 * - Lock Ordering: Always acquire locks in the same order
 * - Consistent ordering: All threads use the same lock sequence
 * - Prevents circular wait: No circular dependency between threads
 * 
 * Why needed in e-commerce:
 * - Multiple resources: Inventory, payment, shipping need coordination
 * - Prevent system hang: Deadlocks cause entire system to freeze
 * - Ensure reliability: Orders must process reliably
 * - Better user experience: No indefinite waiting
 * 
 * Safety Measure:
 * - Lock ordering: Always acquire locks in consistent order
 * - Lock hierarchy: Define a fixed order (e.g., Inventory → Payment)
 * - All threads follow same order: Prevents circular wait
 * - Alternative: Use timeout-based locks (tryLock with timeout)
 * 
 * Interview Tip:
 * - Always acquire locks in the same order
 * - Use consistent lock hierarchy across all threads
 * - Consider using ReentrantLock with tryLock(timeout)
 * - Design to minimize lock dependencies
 * - Use lock-free data structures when possible
 */
public class OrderProcessorWithLockOrdering implements Runnable {
    
    private final Order order;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final int workerId;
    
    public OrderProcessorWithLockOrdering(Order order, 
                                         InventoryService inventoryService,
                                         PaymentService paymentService,
                                         int workerId) {
        this.order = order;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.workerId = workerId;
    }
    
    @Override
    public void run() {
        try {
            Thread.currentThread().setName("Worker-" + workerId);
            System.out.println("[Worker-" + workerId + "] Starting: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // ✅ ALWAYS use the same lock order: Inventory → Payment
            // This prevents deadlock because all threads acquire locks in the same order
            processWithConsistentLockOrder();
            
        } catch (Exception e) {
            System.out.println("[Worker-" + workerId + "] ✗ Error: " + e.getMessage());
            order.setStatus(Order.OrderStatus.FAILED);
        }
    }
    
    /**
     * Process with consistent lock order: Inventory → Payment
     * ✅ All threads use the same order → No deadlock
     * 
     * Lock Ordering Rule:
     * 1. Always lock InventoryService first
     * 2. Then lock PaymentService
     * 3. Never reverse this order
     * 
     * Why this works:
     * - All threads acquire locks in the same sequence
     * - No circular wait possible
     * - If Thread 1 has InventoryService, Thread 2 waits (doesn't hold PaymentService)
     * - When Thread 1 releases, Thread 2 can proceed
     */
    private void processWithConsistentLockOrder() {
        // ✅ Step 1: ALWAYS lock InventoryService first
        synchronized (inventoryService.getLock()) {
            System.out.println("[Worker-" + workerId + "] Locked InventoryService");
            
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Worker-" + workerId + "] ✗ Insufficient stock");
                order.setStatus(Order.OrderStatus.FAILED);
                return; // Release lock automatically
            }
            
            // ✅ Step 2: Then lock PaymentService (consistent order)
            synchronized (paymentService.getLock()) {
                System.out.println("[Worker-" + workerId + "] Locked PaymentService");
                
                boolean paid = paymentService.processPayment(order.getUserId(), order.getPrice());
                if (!paid) {
                    System.out.println("[Worker-" + workerId + "] ✗ Payment failed");
                    order.setStatus(Order.OrderStatus.FAILED);
                    return; // Release locks automatically
                }
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            } // PaymentService lock released
        } // InventoryService lock released
    }
    
    /**
     * Alternative approach: Using lock ordering based on resource ID.
     * ✅ Orders locks by resource identifier to ensure consistent ordering
     */
    private void processWithResourceBasedOrdering() {
        // ✅ Order locks by resource identifier (e.g., hash code)
        Object lock1 = inventoryService.getLock();
        Object lock2 = paymentService.getLock();
        
        // Ensure consistent ordering (always lock the "smaller" one first)
        if (System.identityHashCode(lock1) > System.identityHashCode(lock2)) {
            Object temp = lock1;
            lock1 = lock2;
            lock2 = temp;
        }
        
        synchronized (lock1) {
            synchronized (lock2) {
                // Process order...
            }
        }
    }
}