package com.inventoryProcessing.Concept8;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * CONCEPT #8 - AFTER: OrderQueue using BlockingQueue
 * 
 * ✅ THIS VERSION USES THREAD-SAFE COLLECTIONS (EFFICIENT) ✅
 * 
 * Definition:
 * - BlockingQueue: Thread-safe queue with built-in blocking operations
 * - put(): Blocks if queue is full
 * - take(): Blocks if queue is empty
 * - No need for manual wait/notify
 * 
 * Why needed in e-commerce:
 * - Producer-Consumer pattern: Built-in blocking operations
 * - No manual synchronization: Thread-safe by design
 * - Better performance: Optimized for concurrent access
 * - Cleaner code: No need for wait/notify
 * 
 * Safety Measure:
 * - BlockingQueue: Thread-safe with built-in blocking
 * - Atomic operations: put(), take(), offer(), poll() are atomic
 * - No external synchronization needed
 * - Handles producer-consumer coordination automatically
 * 
 * Interview Tip:
 * - BlockingQueue: Perfect for producer-consumer pattern
 * - LinkedBlockingQueue: Unbounded or bounded queue
 * - ArrayBlockingQueue: Fixed-size array-based queue
 * - PriorityBlockingQueue: Priority-ordered queue
 * - SynchronousQueue: Handoff queue (no storage)
 */
public class OrderQueue {
    
    // ✅ BlockingQueue: Thread-safe with built-in blocking operations
    // No need for manual wait/notify
    // Handles producer-consumer coordination automatically
    private final BlockingQueue<Order> queue;
    private volatile boolean shutdown = false;
    
    /**
     * Create order queue with maximum size.
     * 
     * @param maxSize Maximum number of orders in queue
     */
    public OrderQueue(int maxSize) {
        // ✅ LinkedBlockingQueue: Thread-safe, bounded queue
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }
    
    /**
     * Add order to queue (Producer operation).
     * ✅ put() blocks if queue is full (no manual wait/notify needed)
     * 
     * How it works:
     * - If queue has space: Adds immediately
     * - If queue is full: Blocks until space available
     * - Thread-safe: No external synchronization needed
     */
    public void enqueue(Order order) throws InterruptedException {
        if (shutdown) {
            throw new InterruptedException("Queue is shutting down");
        }
        
        // ✅ put() blocks if queue is full
        // Automatically handles wait/notify internally
        queue.put(order);
        System.out.println("[Queue] Added: " + order.getOrderId() + " (Size: " + queue.size() + ")");
    }
    
    /**
     * Remove order from queue (Consumer operation).
     * ✅ take() blocks if queue is empty (no manual wait/notify needed)
     * 
     * How it works:
     * - If queue has items: Removes immediately
     * - If queue is empty: Blocks until item available
     * - Thread-safe: No external synchronization needed
     */
    public Order dequeue() throws InterruptedException {
        if (shutdown && queue.isEmpty()) {
            return null; // Signal to stop
        }
        
        // ✅ take() blocks if queue is empty
        // Automatically handles wait/notify internally
        Order order = queue.take();
        System.out.println("[Queue] Removed: " + order.getOrderId() + " (Size: " + queue.size() + ")");
        return order;
    }
    
    /**
     * Non-blocking offer (returns false if queue is full).
     * ✅ Useful when you don't want to block
     */
    public boolean offer(Order order) {
        boolean added = queue.offer(order);
        if (added) {
            System.out.println("[Queue] Offered: " + order.getOrderId() + " (Size: " + queue.size() + ")");
        } else {
            System.out.println("[Queue] Queue full, could not add: " + order.getOrderId());
        }
        return added;
    }
    
    /**
     * Non-blocking poll (returns null if queue is empty).
     * ✅ Useful when you don't want to block
     */
    public Order poll() {
        Order order = queue.poll();
        if (order != null) {
            System.out.println("[Queue] Polled: " + order.getOrderId() + " (Size: " + queue.size() + ")");
        }
        return order;
    }
    
    /**
     * Get current queue size.
     * ✅ Thread-safe, no synchronization needed
     */
    public int size() {
        return queue.size();
    }
    
    /**
     * Check if queue is empty.
     * ✅ Thread-safe, no synchronization needed
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * Shutdown the queue.
     */
    public void shutdown() {
        this.shutdown = true;
        // Wake up any waiting threads
        // Note: BlockingQueue doesn't have a direct shutdown mechanism
        // We use the shutdown flag and add a dummy order to wake up consumers
        queue.offer(new Order("SHUTDOWN", "SYSTEM", "SHUTDOWN", 0, 0.0));
    }
    
    public boolean isShutdown() {
        return shutdown;
    }
}

