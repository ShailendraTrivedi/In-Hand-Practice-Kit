package com.inventoryProcessing.Concept9;

/**
 * CONCEPT #9 - BEFORE: OrderProcessor WITH Deadlock Risk
 * 
 * ⚠️  THIS VERSION CAN CAUSE DEADLOCK ⚠️
 * 
 * Problem:
 * - Thread 1: Locks InventoryService, then tries to lock PaymentService
 * - Thread 2: Locks PaymentService, then tries to lock InventoryService
 * - Both threads wait for each other → DEADLOCK!
 * 
 * Deadlock Scenario:
 * Thread 1: lock(inventory) → wait for lock(payment)
 * Thread 2: lock(payment) → wait for lock(inventory)
 * Result: Both threads blocked forever
 * 
 * Conditions for Deadlock (all must be true):
 * 1. Mutual Exclusion: Resources are locked
 * 2. Hold and Wait: Threads hold locks while waiting for others
 * 3. No Preemption: Locks cannot be forcibly taken
 * 4. Circular Wait: Threads wait in a circle
 */
public class OrderProcessorWithDeadlock implements Runnable {
    
    private final Order order;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final int workerId;
    private final boolean lockOrder1; // Determines lock order
    
    /**
     * Create order processor.
     * 
     * @param lockOrder1 If true, locks inventory first, then payment
     *                   If false, locks payment first, then inventory
     *                   ⚠️  Different lock orders cause deadlock!
     */
    public OrderProcessorWithDeadlock(Order order, 
                                      InventoryService inventoryService,
                                      PaymentService paymentService,
                                      int workerId,
                                      boolean lockOrder1) {
        this.order = order;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.workerId = workerId;
        this.lockOrder1 = lockOrder1;
    }
    
    @Override
    public void run() {
        try {
            Thread.currentThread().setName("Worker-" + workerId);
            System.out.println("[Worker-" + workerId + "] Starting: " + order.getOrderId() + 
                             " (Lock order: " + (lockOrder1 ? "Inventory→Payment" : "Payment→Inventory") + ")");
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            if (lockOrder1) {
                // ⚠️  DEADLOCK RISK: Lock order 1 (Inventory → Payment)
                processWithLockOrder1();
            } else {
                // ⚠️  DEADLOCK RISK: Lock order 2 (Payment → Inventory)
                processWithLockOrder2();
            }
            
        } catch (Exception e) {
            System.out.println("[Worker-" + workerId + "] ✗ Error: " + e.getMessage());
            order.setStatus(Order.OrderStatus.FAILED);
        }
    }
    
    /**
     * Process with lock order 1: Inventory → Payment
     * ⚠️  DEADLOCK RISK if another thread uses order 2
     */
    private void processWithLockOrder1() {
        // ⚠️  Step 1: Lock InventoryService
        synchronized (inventoryService.getLock()) {
            System.out.println("[Worker-" + workerId + "] Locked InventoryService");
            
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            if (!reserved) {
                System.out.println("[Worker-" + workerId + "] ✗ Insufficient stock");
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            // ⚠️  Step 2: Try to lock PaymentService (while holding InventoryService lock)
            // If another thread has PaymentService locked and is waiting for InventoryService,
            // we have a DEADLOCK!
            synchronized (paymentService.getLock()) {
                System.out.println("[Worker-" + workerId + "] Locked PaymentService");
                
                boolean paid = paymentService.processPayment(order.getUserId(), order.getPrice());
                if (!paid) {
                    System.out.println("[Worker-" + workerId + "] ✗ Payment failed");
                    order.setStatus(Order.OrderStatus.FAILED);
                    return;
                }
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            }
        }
    }
    
    /**
     * Process with lock order 2: Payment → Inventory
     * ⚠️  DEADLOCK RISK if another thread uses order 1
     */
    private void processWithLockOrder2() {
        // ⚠️  Step 1: Lock PaymentService
        synchronized (paymentService.getLock()) {
            System.out.println("[Worker-" + workerId + "] Locked PaymentService");
            
            boolean paid = paymentService.processPayment(order.getUserId(), order.getPrice());
            if (!paid) {
                System.out.println("[Worker-" + workerId + "] ✗ Payment failed");
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            // ⚠️  Step 2: Try to lock InventoryService (while holding PaymentService lock)
            // If another thread has InventoryService locked and is waiting for PaymentService,
            // we have a DEADLOCK!
            synchronized (inventoryService.getLock()) {
                System.out.println("[Worker-" + workerId + "] Locked InventoryService");
                
                boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
                if (!reserved) {
                    System.out.println("[Worker-" + workerId + "] ✗ Insufficient stock");
                    order.setStatus(Order.OrderStatus.FAILED);
                    return;
                }
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            }
        }
    }
}