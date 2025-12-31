package com.logprocessing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * STEP 4: Thread-Safe Metrics Collector
 * 
 * This class demonstrates how to safely share state across multiple threads.
 * 
 * KEY CONCEPTS:
 * - AtomicInteger/AtomicLong: Thread-safe counters (lock-free)
 * - ConcurrentHashMap: Thread-safe map for concurrent access
 * - synchronized blocks: For complex operations that need atomicity
 * 
 * WHY THIS DESIGN:
 * - Multiple worker threads update metrics concurrently
 * - Need accurate counts without race conditions
 * - Must be performant (minimal locking overhead)
 * 
 * WHAT WOULD GO WRONG WITHOUT IT:
 * - Lost updates: Two threads increment same counter → one update lost
 * - Data corruption: Map entries lost or corrupted
 * - Incorrect metrics: Reports show wrong numbers
 */
public class ProcessingMetrics {
    
    // AtomicInteger: Thread-safe counter (uses CAS - Compare-And-Swap)
    // No locking needed - very efficient for simple increments
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger warningCount = new AtomicInteger(0);
    private final AtomicInteger infoCount = new AtomicInteger(0);
    
    // AtomicLong for timestamps (64-bit)
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    
    // ConcurrentHashMap: Thread-safe map
    // Multiple threads can read/write concurrently without locking
    private final Map<String, AtomicInteger> logsBySource = new ConcurrentHashMap<>();
    
    // For complex operations, we use synchronized blocks
    private final Object statsLock = new Object();
    private int maxProcessingTime = 0;
    private int minProcessingTime = Integer.MAX_VALUE;
    
    /**
     * Records a processed log with its level.
     * 
     * THREAD SAFETY:
     * - AtomicInteger.incrementAndGet() is atomic (thread-safe)
     * - Multiple threads can call this concurrently without issues
     * 
     * INTERNAL BEHAVIOR:
     * - Thread 1: Reads current value (e.g., 5), increments to 6, writes 6
     * - Thread 2: Reads current value (6), increments to 7, writes 7
     * - No lost updates because increment is atomic
     */
    public void recordProcessed(String logLevel, String source, long processingTimeMs) {
        // Atomic operations - no synchronization needed
        totalProcessed.incrementAndGet();
        totalProcessingTime.addAndGet(processingTimeMs);
        
        // Update level-specific counters
        switch (logLevel.toUpperCase()) {
            case "ERROR":
                errorCount.incrementAndGet();
                break;
            case "WARNING":
                warningCount.incrementAndGet();
                break;
            case "INFO":
                infoCount.incrementAndGet();
                break;
        }
        
        // ConcurrentHashMap: Thread-safe putIfAbsent + increment pattern
        // If source doesn't exist, create new counter
        // If exists, increment existing counter
        logsBySource.computeIfAbsent(source, k -> new AtomicInteger(0))
                   .incrementAndGet();
        
        // For complex operations (min/max), use synchronized block
        // This ensures the entire read-check-update is atomic
        synchronized (statsLock) {
            if (processingTimeMs > maxProcessingTime) {
                maxProcessingTime = (int) processingTimeMs;
            }
            if (processingTimeMs < minProcessingTime) {
                minProcessingTime = (int) processingTimeMs;
            }
        }
    }
    
    /**
     * Records an error during processing.
     */
    public void recordError(String source) {
        errorCount.incrementAndGet();
        totalProcessed.incrementAndGet();
        logsBySource.computeIfAbsent(source, k -> new AtomicInteger(0))
                   .incrementAndGet();
    }
    
    /**
     * Gets a snapshot of current metrics.
     * 
     * THREAD SAFETY:
     * - Individual reads are safe (AtomicInteger.get() is thread-safe)
     * - But snapshot might be slightly inconsistent (metrics change during read)
     * - For production, might want to synchronize entire snapshot
     */
    public MetricsSnapshot getSnapshot() {
        synchronized (statsLock) {
            return new MetricsSnapshot(
                totalProcessed.get(),
                errorCount.get(),
                warningCount.get(),
                infoCount.get(),
                totalProcessingTime.get(),
                maxProcessingTime,
                minProcessingTime == Integer.MAX_VALUE ? 0 : minProcessingTime,
                new ConcurrentHashMap<>(logsBySource) // Copy for safety
            );
        }
    }
    
    /**
     * Immutable snapshot of metrics at a point in time.
     */
    public static class MetricsSnapshot {
        private final int totalProcessed;
        private final int errorCount;
        private final int warningCount;
        private final int infoCount;
        private final long totalProcessingTime;
        private final int maxProcessingTime;
        private final int minProcessingTime;
        private final Map<String, AtomicInteger> logsBySource;
        
        public MetricsSnapshot(int totalProcessed, int errorCount, int warningCount,
                              int infoCount, long totalProcessingTime,
                              int maxProcessingTime, int minProcessingTime,
                              Map<String, AtomicInteger> logsBySource) {
            this.totalProcessed = totalProcessed;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.infoCount = infoCount;
            this.totalProcessingTime = totalProcessingTime;
            this.maxProcessingTime = maxProcessingTime;
            this.minProcessingTime = minProcessingTime;
            this.logsBySource = logsBySource;
        }
        
        public int getTotalProcessed() { return totalProcessed; }
        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
        public int getInfoCount() { return infoCount; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public int getMaxProcessingTime() { return maxProcessingTime; }
        public int getMinProcessingTime() { return minProcessingTime; }
        public Map<String, AtomicInteger> getLogsBySource() { return logsBySource; }
        
        public double getAverageProcessingTime() {
            return totalProcessed > 0 ? (double) totalProcessingTime / totalProcessed : 0.0;
        }
        
        public double getErrorRate() {
            return totalProcessed > 0 ? (double) errorCount / totalProcessed * 100 : 0.0;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n=== Processing Metrics ===\n");
            sb.append(String.format("Total Processed: %d\n", totalProcessed));
            sb.append(String.format("  - Errors: %d (%.2f%%)\n", errorCount, getErrorRate()));
            sb.append(String.format("  - Warnings: %d\n", warningCount));
            sb.append(String.format("  - Info: %d\n", infoCount));
            sb.append(String.format("Avg Processing Time: %.2f ms\n", getAverageProcessingTime()));
            sb.append(String.format("Min/Max Processing Time: %d / %d ms\n", minProcessingTime, maxProcessingTime));
            sb.append("\nLogs by Source:\n");
            logsBySource.forEach((source, count) -> 
                sb.append(String.format("  %s: %d\n", source, count.get()))
            );
            return sb.toString();
        }
    }
}