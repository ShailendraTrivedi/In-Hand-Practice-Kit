package com.inventoryProcessing.Concept4;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CONCEPT #4: OrderProducer
 * 
 * Producer thread that adds orders to the queue.
 * Demonstrates producer side of producer-consumer pattern.
 */
public class OrderProducer implements Runnable {
    
    private final OrderQueue queue;
    private final int producerId;
    private final int ordersToProduce;
    private final AtomicInteger ordersProduced = new AtomicInteger(0);
    
    /**
     * Create order producer.
     * 
     * @param queue Queue to add orders to
     * @param producerId Unique identifier for this producer
     * @param ordersToProduce Number of orders to produce
     */
    public OrderProducer(OrderQueue queue, int producerId, int ordersToProduce) {
        this.queue = queue;
        this.producerId = producerId;
        this.ordersToProduce = ordersToProduce;
    }
    
    /**
     * Produce orders and add them to queue.
     * ✅ Uses wait/notify when queue is full
     */
    @Override
    public void run() {
        Thread.currentThread().setName("Producer-" + producerId);
        
        try {
            for (int i = 0; i < ordersToProduce; i++) {
                // Generate random order
                String orderId = "ORD-" + String.format("%03d", ordersProduced.incrementAndGet());
                String userId = "USER-" + ThreadLocalRandom.current().nextInt(100, 999);
                String productId = "PROD-00" + ThreadLocalRandom.current().nextInt(1, 6);
                int quantity = ThreadLocalRandom.current().nextInt(1, 5);
                double price = ThreadLocalRandom.current().nextDouble(10.0, 500.0);
                
                Order order = new Order(orderId, userId, productId, quantity, price);
                
                // ✅ Add to queue (will wait if queue is full)
                queue.enqueue(order);
                
                // Simulate time between order submissions
                Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));
            }
            
            System.out.println("[Producer-" + producerId + "] ✓ Finished producing " + ordersToProduce + " orders");
            
        } catch (InterruptedException e) {
            System.out.println("[Producer-" + producerId + "] ✗ Interrupted");
            Thread.currentThread().interrupt();
        }
    }
    
    public int getOrdersProduced() {
        return ordersProduced.get();
    }

    public int getProducerId() {
        return producerId;
    }
}