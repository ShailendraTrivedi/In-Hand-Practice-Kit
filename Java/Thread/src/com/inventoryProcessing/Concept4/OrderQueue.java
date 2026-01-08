package com.inventoryProcessing.Concept4;

import java.util.LinkedList;
import java.util.Queue;

/**
 * CONCEPT #4 - AFTER: OrderQueue WITH wait/notifyAll
 * 
 * ✅ THIS VERSION IS EFFICIENT ✅
 * 
 * Definition:
 * - wait(): Makes thread wait and releases lock until notified
 * - notifyAll(): Wakes up all waiting threads
 * - Producer-Consumer: Efficient coordination between producers and consumers
 * 
 * Why needed in e-commerce:
 * - Orders arrive asynchronously (producers)
 * - Workers process orders (consumers)
 * - Need efficient coordination without wasting CPU
 * - Consumers should sleep when queue is empty
 * - Producers should sleep when queue is full
 * 
 * Safety Measure:
 * - wait()/notifyAll(): Efficient thread coordination
 * - Must be called within synchronized block
 * - wait() releases lock, allowing other threads to proceed
 * - notifyAll() wakes waiting threads after lock is released
 * 
 * Interview Tip:
 * - Always use wait() in a while loop (spurious wakeups)
 * - Use notifyAll() instead of notify() for multiple consumers
 * - wait() releases the lock atomically
 * - Thread must own the monitor (be in synchronized block)
 */
public class OrderQueue {
    
    private final Queue<Order> queue;
    private final int maxSize;
    private volatile boolean shutdown = false; // For graceful shutdown (Concept #12)
    
    /**
     * Create order queue with maximum size.
     * 
     * @param maxSize Maximum number of orders in queue
     */
    public OrderQueue(int maxSize) {
        this.queue = new LinkedList<>();
        this.maxSize = maxSize;
    }
    
    /**
     * Add order to queue (Producer operation).
     * ✅ Uses wait() when queue is full - thread sleeps instead of busy-waiting
     * 
     * How it works:
     * 1. Thread acquires lock (synchronized block)
     * 2. If queue is full, calls wait() → releases lock and sleeps
     * 3. When space available, another thread calls notifyAll()
     * 4. Waiting thread wakes up, re-acquires lock, and continues
     */
    public void enqueue(Order order) throws InterruptedException {
        synchronized (this) {
            // ✅ EFFICIENT: Wait (sleep) when queue is full
            // Thread releases lock and goes to sleep (no CPU usage)
            while (queue.size() >= maxSize && !shutdown) {
                System.out.println("[Queue] Full! Producer waiting... (Size: " + queue.size() + ")");
                wait(); // Releases lock and waits
            }
            
            if (shutdown) {
                throw new InterruptedException("Queue is shutting down");
            }
            
            queue.offer(order);
            System.out.println("[Queue] Added: " + order.getOrderId() + " (Size: " + queue.size() + ")");
            
            // ✅ Notify waiting consumers that order is available
            notifyAll(); // Wakes up all waiting consumer threads
        }
    }
    
    /**
     * Remove order from queue (Consumer operation).
     * ✅ Uses wait() when queue is empty - thread sleeps instead of busy-waiting
     * 
     * How it works:
     * 1. Thread acquires lock (synchronized block)
     * 2. If queue is empty, calls wait() → releases lock and sleeps
     * 3. When order arrives, producer calls notifyAll()
     * 4. Waiting thread wakes up, re-acquires lock, and continues
     */
    public Order dequeue() throws InterruptedException {
        synchronized (this) {
            // ✅ EFFICIENT: Wait (sleep) when queue is empty
            // Thread releases lock and goes to sleep (no CPU usage)
            // Use while loop to handle spurious wakeups
            while (queue.isEmpty() && !shutdown) {
                System.out.println("[Queue] Empty! Consumer waiting...");
                wait(); // Releases lock and waits
            }
            
            if (shutdown && queue.isEmpty()) {
                return null; // Signal to stop
            }
            
            Order order = queue.poll();
            if (order != null) {
                System.out.println("[Queue] Removed: " + order.getOrderId() + " (Size: " + queue.size() + ")");
            }
            
            // ✅ Notify waiting producers that space is available
            notifyAll(); // Wakes up all waiting producer threads
            
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
    
    /**
     * Shutdown the queue (for graceful shutdown).
     */
    public synchronized void shutdown() {
        this.shutdown = true;
        notifyAll(); // Wake up all waiting threads
    }
    
    /**
     * Check if queue is shutdown.
     */
    public boolean isShutdown() {
        return shutdown;
    }
}