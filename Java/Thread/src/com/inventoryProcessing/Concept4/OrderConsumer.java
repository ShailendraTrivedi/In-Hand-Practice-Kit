package com.inventoryProcessing.Concept4;

/**
 * CONCEPT #4: OrderConsumer
 * 
 * Consumer thread that processes orders from the queue.
 * Demonstrates consumer side of producer-consumer pattern.
 */
public class OrderConsumer implements Runnable {
    
    private final OrderQueue queue;
    private final int consumerId;
    private final InventoryService inventoryService;
    private int ordersProcessed = 0;
    
    /**
     * Create order consumer.
     * 
     * @param queue Queue to consume orders from
     * @param consumerId Unique identifier for this consumer
     * @param inventoryService Inventory service for order processing
     */
    public OrderConsumer(OrderQueue queue, int consumerId, InventoryService inventoryService) {
        this.queue = queue;
        this.consumerId = consumerId;
        this.inventoryService = inventoryService;
    }
    
    /**
     * Consume orders from queue and process them.
     * ✅ Uses wait/notify when queue is empty
     */
    @Override
    public void run() {
        Thread.currentThread().setName("Consumer-" + consumerId);
        
        try {
            while (true) {
                // ✅ Get order from queue (will wait if queue is empty)
                Order order = queue.dequeue();
                
                // Check for shutdown signal
                if (order == null && queue.isShutdown()) {
                    System.out.println("[Consumer-" + consumerId + "] Queue shutdown, stopping...");
                    break;
                }
                
                if (order != null) {
                    // Process the order
                    processOrder(order);
                    ordersProcessed++;
                }
            }
            
            System.out.println("[Consumer-" + consumerId + "] ✓ Processed " + ordersProcessed + " orders");
            
        } catch (InterruptedException e) {
            System.out.println("[Consumer-" + consumerId + "] ✗ Interrupted");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Process a single order.
     */
    private void processOrder(Order order) throws InterruptedException {
        System.out.println("[Consumer-" + consumerId + "] Processing: " + order.getOrderId());
        order.setStatus(Order.OrderStatus.PROCESSING);
        
        // Check inventory
        boolean reserved = inventoryService.reserveStock(order.getProductId(), order.getQuantity());
        if (!reserved) {
            System.out.println("[Consumer-" + consumerId + "] ✗ Insufficient stock: " + order.getOrderId());
            order.setStatus(Order.OrderStatus.FAILED);
            return;
        }
        
        // Simulate payment processing
        Thread.sleep(100);
        
        // Simulate shipping preparation
        Thread.sleep(50);
        
        order.setStatus(Order.OrderStatus.COMPLETED);
        System.out.println("[Consumer-" + consumerId + "] ✓ Completed: " + order.getOrderId());
    }
    
    public int getOrdersProcessed() {
        return ordersProcessed;
    }   

    public int getConsumerId() {
        return consumerId;
    }
}