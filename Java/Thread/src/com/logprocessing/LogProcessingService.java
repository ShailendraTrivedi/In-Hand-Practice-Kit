package com.logprocessing;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * STEP 8: Performance-Optimized Log Processing Service
 * 
 * This demonstrates:
 * - Optimal thread pool sizing for I/O-bound tasks
 * - Performance monitoring and metrics
 * - Preventing worker starvation
 * - Fair task distribution
 * 
 * KEY CONCEPTS:
 * - CPU-bound vs I/O-bound: Different pool sizing strategies
 * - Thread utilization: Monitor active vs idle threads
 * - Starvation prevention: Fair scheduling, work stealing
 * - Performance tuning: Measure and optimize based on metrics
 * 
 * WHY THIS DESIGN:
 * - Log processing is I/O-bound (parsing, DB writes, network)
 * - Need optimal pool size for throughput
 * - Prevent some threads from starving
 * - Monitor performance for tuning
 * 
 * WHAT WOULD GO WRONG WITHOUT IT:
 * - Underutilized CPU (too few threads)
 * - Context switching overhead (too many threads)
 * - Worker starvation (unfair distribution)
 * - Poor performance (wrong pool size)
 */
public class LogProcessingService {
    
    private final ThreadPoolExecutor executorService;
    private final LogQueue logQueue;
    private final ProcessingMetrics metrics;
    private final AlertEvaluationService alertService;
    
    private volatile boolean running = true;
    private final CountDownLatch shutdownLatch;
    private final int poolSize;
    
    private final List<Log> logBatch = new ArrayList<>();
    private static final int BATCH_SIZE = 10;
    private final Object batchLock = new Object();
    
    // Performance monitoring
    private final LongAdder tasksSubmitted = new LongAdder();
    private final LongAdder tasksCompleted = new LongAdder();
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicInteger maxQueueSize = new AtomicInteger(0);
    
    // Thread utilization tracking
    private final AtomicInteger peakActiveThreads = new AtomicInteger(0);
    private final AtomicInteger peakQueueSize = new AtomicInteger(0);
    
    /**
     * Creates a log processing service with optimal thread pool sizing.
     * 
     * POOL SIZING STRATEGY:
     * - Log processing is I/O-bound (parsing, validation, DB writes)
     * - Formula: CPU cores × 2 (good starting point for I/O-bound)
     * - Can be tuned based on actual wait/compute ratio
     * - Use LinkedBlockingQueue for fair task distribution
     */
    public LogProcessingService(int poolSize, LogQueue logQueue) {
        this.poolSize = poolSize;
        this.logQueue = logQueue;
        this.metrics = new ProcessingMetrics();
        this.alertService = new AlertEvaluationService(metrics);
        this.shutdownLatch = new CountDownLatch(poolSize);
        
        // Create ThreadPoolExecutor with monitoring capabilities
        // LinkedBlockingQueue: Fair FIFO ordering (prevents starvation)
        // RejectedExecutionHandler: Log rejected tasks
        this.executorService = new ThreadPoolExecutor(
            poolSize,                    // Core pool size
            poolSize,                    // Maximum pool size
            60L,                         // Keep-alive time
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), // Fair task queue
            new ThreadPoolExecutor.CallerRunsPolicy() // Execute in caller thread if queue full
        );
        
        System.out.println(
            String.format(
                "[LogProcessingService] Created thread pool: core=%d, max=%d, queue=LinkedBlockingQueue",
                poolSize,
                poolSize
            )
        );
        System.out.println(
            String.format(
                "[LogProcessingService] CPU cores available: %d",
                Runtime.getRuntime().availableProcessors()
            )
        );
        System.out.println(
            String.format(
                "[LogProcessingService] Pool size strategy: I/O-bound (CPU cores × 2 = %d)",
                Runtime.getRuntime().availableProcessors() * 2
            )
        );
        
        // Start performance monitoring thread
        startPerformanceMonitor();
    }
    
    /**
     * Starts a background thread to monitor thread pool performance.
     * 
     * MONITORING METRICS:
     * - Active threads: Currently executing tasks
     * - Queue size: Tasks waiting for threads
     * - Completed tasks: Total tasks finished
     * - Thread utilization: Active / Pool size
     */
    private void startPerformanceMonitor() {
        Thread monitor = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000); // Report every 5 seconds
                    
                    if (!running) break;
                    
                    int activeThreads = executorService.getActiveCount();
                    int queueSize = executorService.getQueue().size();
                    long completed = executorService.getCompletedTaskCount();
                    int poolSize = executorService.getPoolSize();
                    
                    // Update peak metrics
                    if (activeThreads > peakActiveThreads.get()) {
                        peakActiveThreads.set(activeThreads);
                    }
                    if (queueSize > peakQueueSize.get()) {
                        peakQueueSize.set(queueSize);
                    }
                    if (queueSize > maxQueueSize.get()) {
                        maxQueueSize.set(queueSize);
                    }
                    
                    double utilization = poolSize > 0 ? (double) activeThreads / poolSize * 100 : 0;
                    
                    System.out.println(
                        String.format(
                            "\n[Performance Monitor] Active: %d/%d (%.1f%%) | Queue: %d | Completed: %d",
                            activeThreads,
                            poolSize,
                            utilization,
                            queueSize,
                            completed
                        )
                    );
                    
                    // Starvation detection
                    if (queueSize > 0 && activeThreads < poolSize) {
                        System.out.println(
                            String.format(
                                "[Performance Monitor] ⚠️  Potential starvation: %d tasks queued but %d threads idle",
                                queueSize,
                                poolSize - activeThreads
                            )
                        );
                    }
                    
                    // Underutilization detection
                    if (activeThreads < poolSize * 0.5 && queueSize == 0) {
                        System.out.println(
                            String.format(
                                "[Performance Monitor] 💡 Consider reducing pool size: Only %d/%d threads active",
                                activeThreads,
                                poolSize
                            )
                        );
                    }
                    
                    // Overload detection
                    if (queueSize > poolSize * 2) {
                        System.out.println(
                            String.format(
                                "[Performance Monitor] 🚨 High queue size: %d tasks waiting (consider increasing pool size)",
                                queueSize
                            )
                        );
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitor.setName("PerformanceMonitor");
        monitor.setDaemon(true); // Don't prevent JVM shutdown
        monitor.start();
        
        System.out.println("[LogProcessingService] Performance monitor started");
    }
    
    public void start() {
        System.out.println("[LogProcessingService] Starting worker tasks...");
        
        for (int i = 0; i < poolSize; i++) {
            final int workerId = i + 1;
            
            // Submit task and track it for potential cancellation
            Future<?> taskFuture = executorService.submit(() -> {
                System.out.println(
                    String.format(
                        "[Worker Task %d] Started in thread: %s",
                        workerId,
                        Thread.currentThread().getName()
                    )
                );
                
                try {
                    // Main processing loop
                    while (running && !Thread.currentThread().isInterrupted()) {
                        try {
                            // Measure wait time (time spent waiting for logs)
                            long waitStart = System.currentTimeMillis();
                            // takeLog() can throw InterruptedException
                            // This is the proper way to handle blocking operations
                            Log log = logQueue.takeLog();
                            long waitTime = System.currentTimeMillis() - waitStart;
                            totalWaitTime.addAndGet(waitTime);
                            
                            // Check interrupt status again (might have been interrupted during wait)
                            if (Thread.currentThread().isInterrupted()) {
                                System.out.println(
                                    String.format(
                                        "[Worker Task %d] Interrupted after taking log, stopping...",
                                        workerId
                                    )
                                );
                                break;
                            }
                            
                            if (log != null) {
                                if (!running) {
                                    break;
                                }
                                
                                tasksSubmitted.increment();
                                
                                long startTime = System.currentTimeMillis();
                                
                                // Process log (this method checks for interruption)
                                processLog(log, workerId);
                                
                                // Check if we were interrupted during processing
                                if (Thread.currentThread().isInterrupted()) {
                                    System.out.println(
                                        String.format(
                                            "[Worker Task %d] Interrupted during processing, stopping...",
                                            workerId
                                        )
                                    );
                                    break;
                                }
                                
                                long processingTime = System.currentTimeMillis() - startTime;
                                totalProcessingTime.addAndGet(processingTime);
                                
                                metrics.recordProcessed(
                                    log.getLevel(),
                                    log.getSource(),
                                    processingTime
                                );
                                
                                tasksCompleted.increment();
                                addToBatch(log);
                            }
                        } catch (InterruptedException e) {
                            // CRITICAL: InterruptedException means thread was interrupted
                            // We must respond by stopping the loop
                            System.out.println(
                                String.format(
                                    "[Worker Task %d] InterruptedException caught: %s",
                                    workerId,
                                    e.getMessage()
                                )
                            );
                            
                            // CRITICAL: Restore interrupt status
                            // This preserves the interrupt signal for callers
                            Thread.currentThread().interrupt();
                            
                            // Exit the loop
                            break;
                        }
                    }
                } finally {
                    shutdownLatch.countDown();
                    System.out.println(
                        String.format(
                            "[Worker Task %d] Stopped. Thread: %s | Remaining workers: %d",
                            workerId,
                            Thread.currentThread().getName(),
                            shutdownLatch.getCount()
                        )
                    );
                }
            });
        }
        
        System.out.println("[LogProcessingService] All worker tasks submitted to thread pool");
    }
    
    /**
     * Processes a log with simulated I/O-bound work.
     * 
     * I/O-BOUND CHARACTERISTICS:
     * - Thread spends time waiting (I/O operations)
     * - CPU is idle during waits
     * - Can benefit from more threads than CPU cores
     */
    private void processLog(Log log, int workerId) throws InterruptedException {
        // Simulate I/O-bound work:
        // - Parsing (CPU) - 20ms
        // - Database write (I/O wait) - 50ms
        // - Network call (I/O wait) - 30ms
        // - Validation (CPU) - 10ms
        
        try {
            // Parsing (CPU-bound)
            Thread.sleep(20);
            
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Processing interrupted");
            }
            
            // Database write (I/O-bound - thread waits)
            Thread.sleep(50);
            
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Processing interrupted");
            }
            
            // Network call (I/O-bound - thread waits)
            Thread.sleep(30);
            
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Processing interrupted");
            }
            
            // Validation (CPU-bound)
            Thread.sleep(10);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }
    
    private void addToBatch(Log log) {
        synchronized (batchLock) {
            logBatch.add(log);
            
            if (logBatch.size() >= BATCH_SIZE) {
                List<Log> batchToEvaluate = new ArrayList<>(logBatch);
                logBatch.clear();
                
                Future<AlertEvaluationService.AlertResult> future = 
                    alertService.evaluateLogsAsync(batchToEvaluate, "AUTO-" + System.currentTimeMillis());
                
                new Thread(() -> {
                    try {
                        AlertEvaluationService.AlertResult result = future.get();
                        if (result.shouldTriggerAlert()) {
                            System.out.println(
                                String.format(
                                    "\n🚨 ALERT TRIGGERED: %s\n",
                                    result
                                )
                            );
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("[Alert Check] Interrupted, cancelling evaluation...");
                        future.cancel(true);
                    } catch (ExecutionException e) {
                        System.err.println(
                            String.format(
                                "[Alert Check] Evaluation failed: %s",
                                e.getCause().getMessage()
                            )
                        );
                    }
                }).start();
            }
        }
    }
    
    public void shutdown() throws InterruptedException {
        System.out.println("\n[LogProcessingService] Initiating graceful shutdown...");
        running = false;
        
        executorService.shutdown();
        
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("[LogProcessingService] Timeout reached, forcing shutdown...");
                System.out.println("[LogProcessingService] Calling shutdownNow() - will interrupt all threads");
                
                // shutdownNow() interrupts all running tasks
                List<Runnable> pendingTasks = executorService.shutdownNow();
                System.out.println(
                    String.format(
                        "[LogProcessingService] Interrupted %d running tasks, %d pending tasks cancelled",
                        poolSize,
                        pendingTasks.size()
                    )
                );
                
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("[LogProcessingService] Thread pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            System.err.println("[LogProcessingService] Shutdown interrupted");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        if (!shutdownLatch.await(5, TimeUnit.SECONDS)) {
            System.err.println(
                String.format(
                    "[LogProcessingService] Warning: %d workers did not acknowledge shutdown",
                    shutdownLatch.getCount()
                )
            );
        }
        
        synchronized (batchLock) {
            if (!logBatch.isEmpty()) {
                System.out.println(
                    String.format(
                        "[LogProcessingService] Evaluating final batch of %d logs...",
                        logBatch.size()
                    )
                );
                try {
                    Future<AlertEvaluationService.AlertResult> future = 
                        alertService.evaluateLogsAsync(logBatch, "FINAL");
                    AlertEvaluationService.AlertResult result = future.get(5, TimeUnit.SECONDS);
                    System.out.println("[LogProcessingService] Final batch result: " + result);
                } catch (Exception e) {
                    System.err.println("[LogProcessingService] Final batch evaluation failed: " + e.getMessage());
                }
            }
        }
        
        alertService.shutdown();
        
        // Print final performance report
        printPerformanceReport();
        
        System.out.println("[LogProcessingService] Shutdown complete");
    }
    
    /**
     * Prints comprehensive performance report.
     */
    private void printPerformanceReport() {
        System.out.println("\n=== Performance Report ===");
        System.out.println(
            String.format(
                "Pool Size: %d (Core: %d, Max: %d)",
                executorService.getPoolSize(),
                executorService.getCorePoolSize(),
                executorService.getMaximumPoolSize()
            )
        );
        System.out.println(
            String.format(
                "Tasks Submitted: %d",
                tasksSubmitted.sum()
            )
        );
        System.out.println(
            String.format(
                "Tasks Completed: %d",
                tasksCompleted.sum()
            )
        );
        System.out.println(
            String.format(
                "Peak Active Threads: %d/%d (%.1f%%)",
                peakActiveThreads.get(),
                poolSize,
                (double) peakActiveThreads.get() / poolSize * 100
            )
        );
        System.out.println(
            String.format(
                "Peak Queue Size: %d",
                peakQueueSize.get()
            )
        );
        
        if (tasksCompleted.sum() > 0) {
            double avgWaitTime = (double) totalWaitTime.get() / tasksCompleted.sum();
            double avgProcessTime = (double) totalProcessingTime.get() / tasksCompleted.sum();
            double waitRatio = totalWaitTime.get() > 0 ? 
                (double) totalProcessingTime.get() / totalWaitTime.get() : 0;
            
            System.out.println(
                String.format(
                    "Avg Wait Time: %.2f ms",
                    avgWaitTime
                )
            );
            System.out.println(
                String.format(
                    "Avg Process Time: %.2f ms",
                    avgProcessTime
                )
            );
            System.out.println(
                String.format(
                    "Wait/Process Ratio: %.2f (I/O-bound if > 1.0)",
                    waitRatio
                )
            );
            
            // Pool sizing recommendation
            System.out.println("\n=== Pool Sizing Analysis ===");
            if (waitRatio > 1.0) {
                int recommendedSize = (int) (Runtime.getRuntime().availableProcessors() * (1 + waitRatio));
                System.out.println(
                    String.format(
                        "Task is I/O-bound (wait ratio: %.2f)",
                        waitRatio
                    )
                );
                System.out.println(
                    String.format(
                        "Recommended pool size: %d (current: %d)",
                        recommendedSize,
                        poolSize
                    )
                );
            } else {
                System.out.println(
                    String.format(
                        "Task is CPU-bound (wait ratio: %.2f)",
                        waitRatio
                    )
                );
                System.out.println(
                    String.format(
                        "Recommended pool size: %d (CPU cores, current: %d)",
                        Runtime.getRuntime().availableProcessors(),
                        poolSize
                    )
                );
            }
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public ProcessingMetrics.MetricsSnapshot getMetrics() {
        return metrics.getSnapshot();
    }
    
    public AlertEvaluationService getAlertService() {
        return alertService;
    }
    
    public ThreadPoolExecutor getExecutorService() {
        return executorService;
    }
}