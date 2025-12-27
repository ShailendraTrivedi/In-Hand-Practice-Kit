package com.orderprocessing.queue;

import com.orderprocessing.model.Order;
import java.util.LinkedList;
import java.util.Queue;

/**
 * OrderQueue implements a thread-safe queue for orders using wait/notify.
 * 
 * CONCURRENCY CONCEPT #4: wait() / notifyAll() (Producer-Consumer Pattern)
 * 
 * A. Definition:
 *    wait() causes the current thread to wait until another thread calls
 *    notify() or notifyAll() on the same object. This enables efficient
 *    thread coordination without busy-waiting.
 * 
 * B. Why It Is Needed in a Real Backend:
 *    In production, order producers (API endpoints) and consumers (worker
 *    threads) operate at different rates. Without proper coordination,
 *    consumers would waste CPU cycles polling empty queues, or producers
 *    would block unnecessarily when the queue is full, degrading system
 *    performance and scalability.
 * 
 * C. Safety Measure Used:
 *    Synchronized blocks protect the queue operations, and wait/notify
 *    coordinate between producer and consumer threads efficiently.
 * 
 * D. Safety Measure Definition:
 *    wait() must be called inside a synchronized block. When a thread
 *    calls wait(), it releases the lock and enters a waiting state.
 *    notifyAll() wakes all waiting threads, which then compete for the
 *    lock. This prevents busy-waiting and ensures proper thread coordination
 *    while maintaining thread safety through synchronization.
 */
public class OrderQueue {
    private final Queue<Order> queue;
    private final int maxSize;
    private volatile boolean shutdownRequested = false;
    
    /**
     * CONCURRENCY CONCEPT #3: volatile
     * 
     * A. Definition:
     *    volatile ensures that reads and writes to a variable are directly
     *    from/to main memory, providing visibility guarantees across threads.
     * 
     * B. Why It Is Needed in a Real Backend:
     *    Without volatile, threads might cache the shutdownRequested flag
     *    in CPU registers, causing workers to miss shutdown signals and
     *    continue processing indefinitely, preventing graceful shutdown.
     * 
     * C. Safety Measure Used:
     *    shutdownRequested is declared volatile to ensure all threads see
     *    the latest value immediately.
     * 
     * D. Safety Measure Definition:
     *    volatile prevents compiler optimizations that cache variables in
     *    registers. Every read goes to main memory, and every write is
     *    immediately visible to all threads. This ensures the happens-before
     *    relationship for shutdown flags without the overhead of synchronization.
     */
    
    public OrderQueue(int maxSize) {
        this.queue = new LinkedList<>();
        this.maxSize = maxSize;
    }
    
    /**
     * Adds an order to the queue (Producer operation).
     * Blocks if queue is full.
     */
    public synchronized void enqueue(Order order) throws InterruptedException {
        while (queue.size() >= maxSize && !shutdownRequested) {
            wait(); // Wait until space is available
        }
        
        if (shutdownRequested) {
            throw new InterruptedException("Queue is shutting down");
        }
        
        queue.offer(order);
        notifyAll(); // Notify waiting consumers
    }
    
    /**
     * Removes and returns an order from the queue (Consumer operation).
     * Blocks if queue is empty.
     */
    public synchronized Order dequeue() throws InterruptedException {
        while (queue.isEmpty() && !shutdownRequested) {
            wait(); // Wait until an order is available
        }
        
        if (shutdownRequested && queue.isEmpty()) {
            return null; // Signal end of processing
        }
        
        Order order = queue.poll();
        notifyAll(); // Notify waiting producers
        return order;
    }
    
    /**
     * Returns the current queue size.
     */
    public synchronized int size() {
        return queue.size();
    }
    
    /**
     * Checks if the queue is empty.
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * Signals shutdown to all waiting threads.
     */
    public synchronized void shutdown() {
        shutdownRequested = true;
        notifyAll(); // Wake all waiting threads
    }
    
    public boolean isShutdownRequested() {
        return shutdownRequested;
    }
}

