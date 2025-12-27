package com.orderprocessing.manager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ShutdownManager handles system-wide shutdown signals.
 * 
 * CONCURRENCY CONCEPT #9: Deadlock Prevention
 * 
 * A. Definition:
 *    Deadlock occurs when two or more threads are blocked forever,
 *    waiting for each other to release resources. Deadlock prevention
 *    involves design patterns that eliminate the possibility of deadlocks.
 * 
 * B. Why It Is Needed in a Real Backend:
 *    Deadlocks cause the system to hang indefinitely, making it
 *    unresponsive to users. In production, this results in service
 *    outages, lost revenue, and requires manual intervention to restart
 *    services, causing significant downtime.
 * 
 * C. Safety Measure Used:
 *    Consistent lock ordering (always acquire locks in the same order),
 *    timeout-based operations, and avoiding nested locks where possible.
 *    Using atomic flags for shutdown coordination instead of complex
 *    lock hierarchies.
 * 
 * D. Safety Measure Definition:
 *    By always acquiring locks in a consistent order (e.g., always
 *    lock OrderQueue before InventoryService), we prevent circular
 *    wait conditions. Timeout-based operations (Future.get with timeout)
 *    prevent indefinite blocking. Atomic flags (AtomicBoolean) provide
 *    lock-free coordination, eliminating deadlock risk entirely.
 */
public class ShutdownManager {
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    
    /**
     * CONCURRENCY CONCEPT #10: Thread Starvation Prevention
     * 
     * A. Definition:
     *    Thread starvation occurs when a thread cannot gain access to
     *    resources it needs because other threads are consistently
     *    prioritized, causing it to wait indefinitely.
     * 
     * B. Why It Is Needed in a Real Backend:
     *    Starved threads represent wasted resources and can cause
     *    certain types of requests to never be processed, leading to
     *    unfair resource allocation and degraded user experience for
     *    some customers.
     * 
     * C. Safety Measure Used:
     *    Fair thread pool sizing, proper queue management, and using
     *    notifyAll() instead of notify() to wake all waiting threads.
     * 
     * D. Safety Measure Definition:
     *    notifyAll() wakes all threads waiting on a condition, giving
     *    each a fair chance to acquire the lock. Proper thread pool
     *    sizing ensures sufficient worker threads. Queue-based task
     *    distribution (FIFO) ensures fair processing order, preventing
     *    any single thread from monopolizing resources.
     */
    
    public void requestShutdown() {
        shutdownRequested.set(true);
    }
    
    public boolean isShutdownRequested() {
        return shutdownRequested.get();
    }
}

