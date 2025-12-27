package com.orderprocessing.manager;

import com.orderprocessing.model.Order;
import com.orderprocessing.queue.OrderQueue;
import com.orderprocessing.service.InventoryService;
import com.orderprocessing.service.PaymentService;
import com.orderprocessing.worker.OrderProcessor;
import com.orderprocessing.worker.OrderWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OrderManager orchestrates the entire order processing system.
 * 
 * CONCURRENCY CONCEPT #5: ExecutorService (Thread Pool Management)
 * 
 * A. Definition:
 *    ExecutorService provides a higher-level abstraction for managing
 *    thread pools, allowing submission of tasks and automatic thread
 *    lifecycle management.
 * 
 * B. Why It Is Needed in a Real Backend:
 *    Creating threads manually is expensive and can lead to resource
 *    exhaustion. ExecutorService manages a pool of reusable threads,
 *    controls concurrency levels, and provides better resource utilization,
 *    which is essential for handling thousands of concurrent requests
 *    in production systems.
 * 
 * C. Safety Measure Used:
 *    Fixed thread pools with appropriate sizing based on workload type
 *    (CPU-bound vs I/O-bound), and proper shutdown with awaitTermination
 *    to ensure all tasks complete.
 * 
 * D. Safety Measure Definition:
 *    Fixed thread pools limit the maximum number of concurrent threads,
 *    preventing resource exhaustion. Tasks are queued when all threads
 *    are busy. Proper shutdown involves calling shutdown() to stop
 *    accepting new tasks, then awaitTermination() to wait for existing
 *    tasks to complete, ensuring no work is lost during shutdown.
 */
public class OrderManager {
    private final OrderQueue orderQueue;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ExecutorService workerExecutor;
    private final ExecutorService paymentExecutor;
    private final OrderProcessor orderProcessor;
    private final List<OrderWorker> workers;
    private final int numWorkers;
    
    /**
     * CONCURRENCY CONCEPT #11: Performance Tuning (CPU vs IO tasks)
     * 
     * A. Definition:
     *    Performance tuning involves sizing thread pools appropriately
     *    based on whether tasks are CPU-bound or I/O-bound. CPU-bound
     *    tasks benefit from thread count ≈ CPU cores, while I/O-bound
     *    tasks can use more threads since they spend time waiting.
     * 
     * B. Why It Is Needed in a Real Backend:
     *    Incorrect thread pool sizing leads to either underutilization
     *    (too few threads) or context-switching overhead (too many threads),
     *    both degrading system performance and increasing latency under load.
     * 
     * C. Safety Measure Used:
     *    Separate thread pools for CPU-bound workers (order processing)
     *    and I/O-bound tasks (payment processing), sized appropriately.
     * 
     * D. Safety Measure Definition:
     *    Worker pool size is based on CPU cores (CPU-bound work), while
     *    payment pool is larger to handle I/O waits. This maximizes
     *    throughput by keeping CPUs busy while allowing I/O operations
     *    to proceed concurrently without blocking CPU-bound work.
     */
    
    public OrderManager(int queueSize, int numWorkers, int paymentThreads) {
        this.orderQueue = new OrderQueue(queueSize);
        this.inventoryService = new InventoryService();
        this.paymentService = new PaymentService();
        this.numWorkers = numWorkers;
        
        // Worker pool: CPU-bound tasks, size based on CPU cores
        this.workerExecutor = Executors.newFixedThreadPool(
            numWorkers,
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "OrderWorker-" + counter.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                }
            }
        );
        
        // Payment pool: I/O-bound tasks, can use more threads
        this.paymentExecutor = Executors.newFixedThreadPool(
            paymentThreads,
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "PaymentWorker-" + counter.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                }
            }
        );
        
        this.orderProcessor = new OrderProcessor(paymentExecutor, paymentService);
        this.workers = new ArrayList<>();
        
        // Start worker threads
        startWorkers();
    }
    
    /**
     * Starts all worker threads.
     */
    private void startWorkers() {
        for (int i = 0; i < numWorkers; i++) {
            OrderWorker worker = new OrderWorker(
                orderQueue,
                inventoryService,
                paymentService,
                orderProcessor
            );
            workers.add(worker);
            workerExecutor.submit(worker);
        }
    }
    
    /**
     * Submits an order to the queue for processing.
     */
    public void submitOrder(Order order) throws InterruptedException {
        orderQueue.enqueue(order);
    }
    
    /**
     * Cancels an order if it hasn't been processed yet.
     */
    public void cancelOrder(Order order) {
        order.cancel();
    }
    
    public OrderQueue getOrderQueue() {
        return orderQueue;
    }
    
    public InventoryService getInventoryService() {
        return inventoryService;
    }
    
    /**
     * CONCURRENCY CONCEPT #12: Proper Shutdown Handling (Graceful Termination)
     * 
     * A. Definition:
     *    Graceful shutdown ensures that all in-progress tasks complete
     *    before the system terminates, preventing data loss and ensuring
     *    system consistency.
     * 
     * B. Why It Is Needed in a Real Backend:
     *    Abrupt termination can cause orders to be lost mid-processing,
     *    payments to be left in inconsistent states, or inventory to be
     *    incorrectly updated. This leads to financial discrepancies and
     *    customer complaints in production.
     * 
     * C. Safety Measure Used:
     *    Multi-phase shutdown: stop accepting new orders, wait for queue
     *    to drain, interrupt workers, and await thread pool termination
     *    with timeout.
     * 
     * D. Safety Measure Definition:
     *    shutdown() stops accepting new tasks. awaitTermination() waits
     *    for existing tasks to complete with a timeout. If timeout expires,
     *    shutdownNow() forcefully terminates remaining tasks. This ensures
     *    we complete as much work as possible while preventing indefinite
     *    hangs during shutdown.
     */
    public void shutdown(long timeoutSeconds) throws InterruptedException {
        System.out.println("\n[OrderManager] Initiating graceful shutdown...");
        
        // Phase 1: Stop accepting new orders
        orderQueue.shutdown();
        System.out.println("[OrderManager] Stopped accepting new orders");
        
        // Phase 2: Stop worker threads
        for (OrderWorker worker : workers) {
            worker.stop();
        }
        
        // Phase 3: Shutdown worker executor
        workerExecutor.shutdown();
        System.out.println("[OrderManager] Shutting down worker pool...");
        
        // Phase 4: Wait for workers to finish current tasks
        if (!workerExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
            System.out.println("[OrderManager] Worker pool did not terminate, forcing shutdown...");
            workerExecutor.shutdownNow();
            
            // Wait a bit more for forced shutdown
            if (!workerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("[OrderManager] Worker pool did not terminate");
            }
        }
        
        // Phase 5: Shutdown payment executor
        paymentExecutor.shutdown();
        System.out.println("[OrderManager] Shutting down payment pool...");
        
        if (!paymentExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
            System.out.println("[OrderManager] Payment pool did not terminate, forcing shutdown...");
            paymentExecutor.shutdownNow();
            
            if (!paymentExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("[OrderManager] Payment pool did not terminate");
            }
        }
        
        System.out.println("[OrderManager] Shutdown complete");
    }
}

