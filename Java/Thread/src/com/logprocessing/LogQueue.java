package com.logprocessing;

import java.util.LinkedList;
import java.util.Queue;

/**
 * STEP 2: Thread-Safe Log Queue
 * 
 * This queue implements the Producer-Consumer pattern:
 * - Producers (log sources) add logs via addLog()
 * - Consumers (workers) take logs via takeLog()
 * - Uses wait()/notifyAll() for efficient thread coordination
 * 
 * WHY THIS DESIGN:
 * - Decouples log arrival from log processing
 * - Handles bursts of logs gracefully
 * - Prevents worker threads from busy-waiting
 * 
 * WHAT WOULD GO WRONG WITHOUT IT:
 * - Workers would poll constantly (waste CPU)
 * - Lost logs if worker is busy
 * - No buffering during high load
 */
public class LogQueue {
    
    private final Queue<Log> queue;
    private final int maxSize;
    private final Object lock = new Object(); // Monitor for synchronization
    
    public LogQueue(int maxSize) {
        this.queue = new LinkedList<>();
        this.maxSize = maxSize;
    }
    
    /**
     * Producer method: Adds a log to the queue.
     * 
     * INTERNAL BEHAVIOR:
     * 1. Thread acquires lock (synchronized block)
     * 2. If queue is full, thread calls wait() → releases lock, blocks
     * 3. When space available, adds log
     * 4. Calls notifyAll() to wake any waiting consumers
     * 5. Releases lock
     * 
     * THREAD BEHAVIOR:
     * - If queue full: Producer thread BLOCKS (waits)
     * - If queue has space: Producer adds log and CONTINUES
     */
    public void addLog(Log log) throws InterruptedException {
        synchronized (lock) {
            // Wait while queue is full
            // This is a "guarded wait" - we wait until condition is met
            while (queue.size() >= maxSize) {
                System.out.println(
                    String.format(
                        "[Producer Thread %s] Queue full (%d/%d), waiting...",
                        Thread.currentThread().getName(),
                        queue.size(),
                        maxSize
                    )
                );
                lock.wait(); // Releases lock, thread goes to WAITING state
            }
            
            // Add log to queue
            queue.offer(log);
            System.out.println(
                String.format(
                    "[Producer Thread %s] Added log: %s | Queue size: %d",
                    Thread.currentThread().getName(),
                    log.getId(),
                    queue.size()
                )
            );
            
            // Wake up any waiting consumers
            lock.notifyAll(); // All waiting threads will wake up and compete for lock
        }
    }
    
    /**
     * Consumer method: Takes a log from the queue.
     * 
     * INTERNAL BEHAVIOR:
     * 1. Thread acquires lock
     * 2. If queue is empty, thread calls wait() → releases lock, blocks
     * 3. When log available, removes and returns log
     * 4. Calls notifyAll() to wake any waiting producers
     * 5. Releases lock
     * 
     * THREAD BEHAVIOR:
     * - If queue empty: Consumer thread BLOCKS (waits efficiently)
     * - If queue has logs: Consumer takes log and CONTINUES
     * 
     * WHY wait() IS BETTER THAN POLLING:
     * - Polling: Thread checks queue 1000x/second (wastes CPU)
     * - wait(): Thread sleeps until notified (uses CPU only when needed)
     */
    public Log takeLog() throws InterruptedException {
        synchronized (lock) {
            // Wait while queue is empty
            while (queue.isEmpty()) {
                System.out.println(
                    String.format(
                        "[Consumer Thread %s] Queue empty, waiting for logs...",
                        Thread.currentThread().getName()
                    )
                );
                lock.wait(); // Thread blocks here until notifyAll() is called
            }
            
            // Remove and return log
            Log log = queue.poll();
            System.out.println(
                String.format(
                    "[Consumer Thread %s] Took log: %s | Queue size: %d",
                    Thread.currentThread().getName(),
                    log.getId(),
                    queue.size()
                )
            );
            
            // Wake up any waiting producers (if queue was full)
            lock.notifyAll();
            
            return log;
        }
    }
    
    /**
     * Returns current queue size (for monitoring).
     * Thread-safe because it's synchronized.
     */
    public int size() {
        synchronized (lock) {
            return queue.size();
        }
    }
    
    /**
     * Checks if queue is empty.
     */
    public boolean isEmpty() {
        synchronized (lock) {
            return queue.isEmpty();
        }
    }
}