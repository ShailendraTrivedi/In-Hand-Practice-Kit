package com.inventoryProcessing.Concept4;

import java.util.LinkedList;
import java.util.Queue;

/**
 * CONCEPT #4 - BEFORE: OrderQueue WITHOUT wait/notify
 * 
 * ⚠️  THIS VERSION USES BUSY-WAITING (INEFFICIENT) ⚠️
 * 
 * Problem:
 * - Consumer threads continuously poll the queue (busy-waiting)
 * - Wastes CPU cycles when queue is empty
 * - High CPU usage even when there's no work
 * 
 * Busy-waiting example:
 * while (queue.isEmpty()) {
 *     // Keep checking - wastes CPU!
 * }
 * 
 * This is inefficient because:
 * - CPU is constantly checking the queue
 * - No work is being done, but CPU is 100% utilized
 * - Battery drain on mobile devices
 * - Poor scalability
 */
public class OrderQueueBusyWaiting {
    
    private final Queue<Order> queue;
    private final int maxSize;
    
    /**
     * Create order queue with maximum size.
     * 
     * @param maxSize Maximum number of orders in queue
     */
    public OrderQueueBusyWaiting(int maxSize) {
        this.queue = new LinkedList<>();
        this.maxSize = maxSize;
    }
    
    /**
     * Add order to queue (Producer operation).
     * ⚠️  Uses busy-waiting if queue is full
     */
    public void enqueue(Order order) {
        synchronized (this) {
            // ⚠️  BUSY-WAITING: Continuously checks until space is available
            while (queue.size() >= maxSize) {
                // Keep checking - wastes CPU!
                // In real scenario, this would spin the CPU at 100%
            }
            
            queue.offer(order);
            System.out.println("[Queue] Added: " + order.getOrderId() + " (Size: " + queue.size() + ")");
        }
    }
    
    /**
     * Remove order from queue (Consumer operation).
     * ⚠️  Uses busy-waiting if queue is empty
     */
    public Order dequeue() {
        synchronized (this) {
            // ⚠️  BUSY-WAITING: Continuously checks until order is available
            while (queue.isEmpty()) {
                // Keep checking - wastes CPU!
                // Thread is active but doing nothing useful
            }
            
            Order order = queue.poll();
            System.out.println("[Queue] Removed: " + order.getOrderId() + " (Size: " + queue.size() + ")");
            return order;
        }
    }
    
    /**
     * Get current queue size.
     */
    public synchronized int size() {
        return queue.size();
    }
    
    /**
     * Check if queue is empty.
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}