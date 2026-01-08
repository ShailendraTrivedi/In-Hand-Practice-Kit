package com.inventoryProcessing.Concept2;

/**
 * CONCEPT #2: OrderWorker with Inventory Management
 * 
 * This worker now interacts with shared inventory state.
 * Demonstrates why synchronization is critical.
 */
public class OrderWorkerWithInventory implements Runnable {
    
    private final Order order;
    private final int workerId;
    private final InventoryService inventoryService;
    
    /**
     * Constructor for OrderWorker.
     * 
     * @param order The order to process
     * @param workerId Unique identifier for this worker
     * @param inventoryService Shared inventory service (shared state - needs synchronization)
     */
    public OrderWorkerWithInventory(Order order, int workerId, InventoryService inventoryService) {
        this.order = order;
        this.workerId = workerId;
        this.inventoryService = inventoryService;
    }
    
    /**
     * Process order with inventory check.
     * This method accesses shared state (inventoryService) which requires synchronization.
     */
    @Override
    public void run() {
        try {
            Thread.currentThread().setName("OrderWorker-" + workerId);
            
            System.out.println("[Worker-" + workerId + "] Starting: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.PROCESSING);
            
            // Step 1: Check and reserve inventory
            // ✅ This uses synchronized methods in InventoryService
            System.out.println("[Worker-" + workerId + "] Checking inventory for: " + 
                             order.getProductId() + " (Qty: " + order.getQuantity() + ")");
            
            int stockBefore = inventoryService.getStock(order.getProductId());
            System.out.println("[Worker-" + workerId + "] Stock before: " + stockBefore);
            
            boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
            
            if (!reserved) {
                System.out.println("[Worker-" + workerId + "] ✗ Insufficient stock for: " + order.getProductId());
                order.setStatus(Order.OrderStatus.FAILED);
                return;
            }
            
            int stockAfter = inventoryService.getStock(order.getProductId());
            System.out.println("[Worker-" + workerId + "] ✓ Reserved. Stock after: " + stockAfter);
            
            // Step 2: Process payment (simulate I/O delay)
            System.out.println("[Worker-" + workerId + "] Processing payment: $" + order.getPrice());
            Thread.sleep(50);
            
            // Step 3: Prepare shipping
            System.out.println("[Worker-" + workerId + "] Preparing shipping");
            Thread.sleep(30);
            
            // Step 4: Complete order
            order.setStatus(Order.OrderStatus.COMPLETED);
            System.out.println("[Worker-" + workerId + "] ✓ Completed: " + order.getOrderId());
            
        } catch (InterruptedException e) {
            System.out.println("[Worker-" + workerId + "] ✗ Interrupted: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.FAILED);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("[Worker-" + workerId + "] ✗ Error: " + order.getOrderId() + " - " + e.getMessage());
            order.setStatus(Order.OrderStatus.FAILED);
        }
    }
    
    public Order getOrder() {
        return order;
    }
}