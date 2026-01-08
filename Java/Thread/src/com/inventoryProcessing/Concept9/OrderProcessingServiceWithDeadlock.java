package com.inventoryProcessing.Concept9;

/**
 * CONCEPT #9 - BEFORE: OrderProcessingService WITH Deadlock Risk
 * 
 * ⚠️  THIS VERSION CAN CAUSE DEADLOCK ⚠️
 * 
 * Problem:
 * - Acquires locks in different order in different methods
 * - Thread 1: Locks inventory, then tries to lock payment
 * - Thread 2: Locks payment, then tries to lock inventory
 * - Circular wait condition → DEADLOCK
 * 
 * Deadlock Conditions (all must be true):
 * 1. Mutual Exclusion: Locks prevent concurrent access
 * 2. Hold and Wait: Thread holds one lock while waiting for another
 * 3. No Preemption: Locks cannot be forcibly taken
 * 4. Circular Wait: Thread 1 waits for Thread 2, Thread 2 waits for Thread 1
 * 
 * This code demonstrates condition #4 (circular wait) by acquiring locks in different order.
 */
public class OrderProcessingServiceWithDeadlock {
    
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    
    public OrderProcessingServiceWithDeadlock(InventoryService inventoryService, PaymentService paymentService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
    }
    
    /**
     * Process order - Method 1: Inventory first, then Payment
     * ⚠️  DEADLOCK RISK: Acquires inventory lock, then payment lock
     */
    public void processOrderMethod1(Order order) {
        System.out.println("[Service] Method1 processing: " + order.getOrderId());
        
        // ⚠️  Step 1: Acquire inventory lock
        synchronized (inventoryService.getLock()) {
            System.out.println("[Service] Method1 acquired inventory lock: " + order.getOrderId());
            
            boolean reserved = inventoryService.reserveStockWithLock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Service] Method1 insufficient stock: " + order.getOrderId());
                return;
            }
            
            // ⚠️  Step 2: Try to acquire payment lock (while holding inventory lock)
            // If another thread has payment lock and is waiting for inventory lock → DEADLOCK
            synchronized (paymentService.getLock()) {
                System.out.println("[Service] Method1 acquired payment lock: " + order.getOrderId());
                
                boolean paid = paymentService.processPaymentWithLock(order.getOrderId(), order.getPrice());
                if (paid) {
                    order.setStatus(Order.OrderStatus.COMPLETED);
                    System.out.println("[Service] Method1 completed: " + order.getOrderId());
                } else {
                    order.setStatus(Order.OrderStatus.FAILED);
                }
            }
        }
    }
    
    /**
     * Process order - Method 2: Payment first, then Inventory
     * ⚠️  DEADLOCK RISK: Acquires payment lock, then inventory lock
     * 
     * This creates a circular wait:
     * - Thread 1 (Method1): Has inventory lock, waits for payment lock
     * - Thread 2 (Method2): Has payment lock, waits for inventory lock
     * - Both threads wait forever → DEADLOCK
     */
    public void processOrderMethod2(Order order) {
        System.out.println("[Service] Method2 processing: " + order.getOrderId());
        
        // ⚠️  Step 1: Acquire payment lock (DIFFERENT ORDER!)
        synchronized (paymentService.getLock()) {
            System.out.println("[Service] Method2 acquired payment lock: " + order.getOrderId());
            
            boolean paid = paymentService.processPaymentWithLock(order.getOrderId(), order.getPrice());
            if (!paid) {
                System.out.println("[Service] Method2 payment failed: " + order.getOrderId());
                return;
            }
            
            // ⚠️  Step 2: Try to acquire inventory lock (while holding payment lock)
            // If another thread has inventory lock and is waiting for payment lock → DEADLOCK
            synchronized (inventoryService.getLock()) {
                System.out.println("[Service] Method2 acquired inventory lock: " + order.getOrderId());
                
                boolean reserved = inventoryService.reserveStockWithLock(order.getProductId(), order.getQuantity());
                if (reserved) {
                    order.setStatus(Order.OrderStatus.COMPLETED);
                    System.out.println("[Service] Method2 completed: " + order.getOrderId());
                } else {
                    order.setStatus(Order.OrderStatus.FAILED);
                }
            }
        }
    }
}

