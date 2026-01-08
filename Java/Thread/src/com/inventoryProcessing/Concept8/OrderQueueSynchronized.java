package com.inventoryProcessing.Concept8;

import java.util.LinkedList;
import java.util.Queue;

/**
 * CONCEPT #8 - BEFORE: OrderQueue using synchronized collections
 * 
 * ⚠️  THIS VERSION USES SYNCHRONIZED WRAPPERS (INEFFICIENT) ⚠️
 * 
 * Problem:
 * - Manual synchronization with regular LinkedList
 * - Coarse-grained locking (entire queue is locked)
 * - No built-in blocking operations (must use wait/notify manually)
 * - Lower concurrency
 */
public class OrderQueueSynchronized {
    
    // ⚠️  Regular queue with manual synchronization
    private final Queue<Order> queue = new LinkedList<>();
    private final int maxSize;
    
    public OrderQueueSynchronized(int maxSize) {
        this.maxSize = maxSize;
    }
    
    /**
     * Add order to queue.
     * ⚠️  Must manually implement blocking with wait/notify
     */
    public void enqueue(Order order) throws InterruptedException {
        synchronized (queue) {
            while (queue.size() >= maxSize) {
                queue.wait(); // Manual wait/notify
            }
            queue.offer(order);
            queue.notifyAll();
        }
    }
    
    /**
     * Remove order from queue.
     * ⚠️  Must manually implement blocking with wait/notify
     */
    public Order dequeue() throws InterruptedException {
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait(); // Manual wait/notify
            }
            Order order = queue.poll();
            queue.notifyAll();
            return order;
        }
    }
    
    public int size() {
        synchronized (queue) {
            return queue.size();
        }
    }
    
    public boolean isEmpty() {
        synchronized (queue) {
            return queue.isEmpty();
        }
    }
}

