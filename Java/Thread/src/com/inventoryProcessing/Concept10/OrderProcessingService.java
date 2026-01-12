package com.inventoryProcessing.Concept10;

import java.util.concurrent.*;

/**
 * CONCEPT #10 - AFTER: OrderProcessingService WITH Starvation Prevention
 * 
 * ✅ THIS VERSION PREVENTS THREAD STARVATION ✅
 * 
 * Definition:
 * - Thread Starvation: Some threads get less CPU time due to unfair scheduling
 * - Pool Sizing: Choosing right number of threads based on task characteristics
 * - Task Separation: Separating CPU-bound and I/O-bound tasks into different pools
 * 
 * Why needed in e-commerce:
 * - Different task types: CPU-bound (calculations) vs I/O-bound (network calls)
 * - Fair processing: All orders should be processed, not just some
 * - Optimal resource usage: Match thread count to task characteristics
 * - Prevent starvation: Low-priority tasks shouldn't wait indefinitely
 * 
 * Safety Measure:
 * - Separate thread pools: CPU-bound and I/O-bound tasks in different pools
 * - Proper pool sizing: CPU tasks ≈ CPU cores, I/O tasks can be more
 * - Priority queues: Process high-priority tasks first (but not exclusively)
 * - Fair scheduling: Ensure all tasks get processed
 * 
 * Interview Tip:
 * - CPU-bound tasks: Threads ≈ number of CPU cores
 * - I/O-bound tasks: Threads can be much higher (2x-10x CPU cores)
 * - Separate pools: Prevents I/O tasks from blocking CPU tasks
 * - Monitor pool utilization: Adjust sizes based on metrics
 */
public class OrderProcessingService {
    
    // ✅ Separate thread pools for different task types
    private final ExecutorService cpuBoundExecutor;    // For CPU-intensive tasks
    private final ExecutorService ioBoundExecutor;     // For I/O operations
    private final ExecutorService mixedExecutor;        // For mixed tasks
    
    // ✅ Priority queue for fair processing
    private final ExecutorService priorityExecutor;
    
    /**
     * Create service with properly sized thread pools.
     * 
     * @param cpuCores Number of CPU cores
     */
    public OrderProcessingService(int cpuCores) {
        // ✅ CPU-bound pool: Size ≈ CPU cores
        // CPU tasks don't block, so we don't need more threads than cores
        int cpuPoolSize = cpuCores;
        this.cpuBoundExecutor = Executors.newFixedThreadPool(cpuPoolSize, 
            new ThreadFactory() {
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "CPU-Worker-" + (++count));
                    t.setPriority(Thread.NORM_PRIORITY);
                    return t;
                }
            });
        
        // ✅ I/O-bound pool: Size can be much higher
        // I/O tasks block (waiting for network/database), so we can have more threads
        int ioPoolSize = cpuCores * 4; // 4x CPU cores for I/O tasks
        this.ioBoundExecutor = Executors.newFixedThreadPool(ioPoolSize,
            new ThreadFactory() {
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "IO-Worker-" + (++count));
                    t.setPriority(Thread.NORM_PRIORITY);
                    return t;
                }
            });
        
        // ✅ Mixed task pool: Balanced size
        int mixedPoolSize = cpuCores * 2;
        this.mixedExecutor = Executors.newFixedThreadPool(mixedPoolSize);
        
        // ✅ Priority executor: Uses priority queue for fair processing
        // Note: Must use Comparator with PriorityBlockingQueue because Runnable doesn't implement Comparable
        // Also use execute() instead of submit() because submit() wraps Runnable in FutureTask
        this.priorityExecutor = new ThreadPoolExecutor(
            2,                          // Core pool size
            cpuCores * 2,               // Maximum pool size
            60L, TimeUnit.SECONDS,      // Keep-alive time
            new PriorityBlockingQueue<Runnable>(11, new PriorityTaskComparator()), // Priority queue with Comparator
            new ThreadFactory() {
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Priority-Worker-" + (++count));
                    return t;
                }
            }
        );
        
        System.out.println("[Service] Created thread pools:");
        System.out.println("  - CPU-bound pool: " + cpuPoolSize + " threads");
        System.out.println("  - I/O-bound pool: " + ioPoolSize + " threads");
        System.out.println("  - Mixed pool: " + mixedPoolSize + " threads");
        System.out.println("  - Priority pool: 2-" + (cpuCores * 2) + " threads");
    }
    
    /**
     * Process CPU-bound task.
     * ✅ Uses CPU-bound pool (size ≈ CPU cores)
     */
    public void processCpuBoundTask(Order order) {
        cpuBoundExecutor.submit(() -> {
            try {
                System.out.println("[CPU-Task] Processing: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.PROCESSING);
                
                // CPU-intensive calculation
                calculateInventory(order);
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[CPU-Task] ✓ Completed: " + order.getOrderId());
                
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[CPU-Task] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Process I/O-bound task.
     * ✅ Uses I/O-bound pool (larger size for blocking operations)
     */
    public void processIoBoundTask(Order order) {
        ioBoundExecutor.submit(() -> {
            try {
                System.out.println("[I/O-Task] Processing: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.PROCESSING);
                
                // I/O operation (doesn't block CPU pool)
                processPayment(order);
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[I/O-Task] ✓ Completed: " + order.getOrderId());
                
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[I/O-Task] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Process order with priority.
     * ✅ Uses priority executor for fair processing
     * Note: Uses execute() instead of submit() because PriorityBlockingQueue
     * requires Comparable tasks, and submit() wraps Runnable in FutureTask
     */
    public void processOrderWithPriority(Order order) {
        priorityExecutor.execute(new PriorityTask(order));
    }
    
    /**
     * Process mixed task.
     * ✅ Uses mixed pool (balanced size)
     */
    public void processOrder(Order order) {
        mixedExecutor.submit(() -> {
            try {
                System.out.println("[Mixed-Task] Processing: " + order.getOrderId());
                order.setStatus(Order.OrderStatus.PROCESSING);
                
                // CPU-bound work (uses CPU pool)
                calculateInventory(order);
                
                // I/O-bound work (uses I/O pool)
                processPayment(order);
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Mixed-Task] ✓ Completed: " + order.getOrderId());
                
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[Mixed-Task] ✗ Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * CPU-intensive calculation.
     */
    private void calculateInventory(Order order) {
        // Simulate CPU-intensive work
        long result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += i * order.getQuantity();
        }
        // Result used for simulation (prevent optimization)
        if (result < 0) {
            System.out.println("Unexpected result");
        }
    }
    
    /**
     * I/O operation (simulates network/database call).
     */
    private void processPayment(Order order) throws InterruptedException {
        // Simulate I/O wait (network call, database query)
        Thread.sleep(200); // Blocks thread during I/O
    }
    
    /**
     * Priority task for priority executor.
     */
    private static class PriorityTask implements Runnable {
        private final Order order;
        private final long timestamp;
        
        public PriorityTask(Order order) {
            this.order = order;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Order getOrder() {
            return order;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("[Priority-Task] Processing: " + order.getOrderId() + 
                                 " (Priority: " + order.getPriority() + ")");
                order.setStatus(Order.OrderStatus.PROCESSING);
                
                // Process order
                Thread.sleep(100);
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                System.out.println("[Priority-Task] ✓ Completed: " + order.getOrderId());
                
            } catch (Exception e) {
                order.setStatus(Order.OrderStatus.FAILED);
                System.err.println("[Priority-Task] ✗ Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Comparator for PriorityTask.
     * ✅ Compares by priority, then by timestamp (FIFO for same priority)
     */
    private static class PriorityTaskComparator implements java.util.Comparator<Runnable> {
        @Override
        public int compare(Runnable r1, Runnable r2) {
            if (!(r1 instanceof PriorityTask) || !(r2 instanceof PriorityTask)) {
                return 0; // Fallback for non-PriorityTask runnables
            }
            
            PriorityTask task1 = (PriorityTask) r1;
            PriorityTask task2 = (PriorityTask) r2;
            
            // Higher priority first
            int priorityCompare = task2.getOrder().getPriority().compareTo(task1.getOrder().getPriority());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            // Same priority: FIFO (older first)
            return Long.compare(task1.getTimestamp(), task2.getTimestamp());
        }
    }
    
    /**
     * Shutdown all executors.
     */
    public void shutdown() throws InterruptedException {
        System.out.println("[Service] Shutting down all thread pools...");
        
        cpuBoundExecutor.shutdown();
        ioBoundExecutor.shutdown();
        mixedExecutor.shutdown();
        priorityExecutor.shutdown();
        
        cpuBoundExecutor.awaitTermination(10, TimeUnit.SECONDS);
        ioBoundExecutor.awaitTermination(10, TimeUnit.SECONDS);
        mixedExecutor.awaitTermination(10, TimeUnit.SECONDS);
        priorityExecutor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("[Service] ✓ All pools shut down");
    }
}