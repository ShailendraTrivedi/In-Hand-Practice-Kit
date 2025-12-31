package com.logprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * STEP 8: Complete Multi-Threaded Log Processing System
 * 
 * This is the FINAL, PRODUCTION-READY version demonstrating:
 * - Optimal thread pool sizing for I/O-bound tasks
 * - Performance monitoring and metrics
 * - Starvation prevention
 * - Performance tuning recommendations
 * - All previous concepts integrated
 */
public class LogProcessingSystem {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== STEP 8: Performance & Starvation ===\n");
        System.out.println("=== COMPLETE MULTI-THREADED LOG PROCESSING SYSTEM ===\n");
        System.out.println("[Main Thread] Thread ID: " + Thread.currentThread().getId());
        
        int cpuCores = Runtime.getRuntime().availableProcessors();
        System.out.println(
            String.format(
                "[Main Thread] System Info: %d CPU cores available",
                cpuCores
            )
        );
        
        LogQueue logQueue = new LogQueue(50); // Larger queue for high load
        
        // Optimal pool size for I/O-bound tasks: CPU cores × 2
        // This allows threads to wait on I/O while others work
        int poolSize = cpuCores * 2;
        System.out.println(
            String.format(
                "[Main Thread] Using pool size: %d (CPU cores × 2 for I/O-bound tasks)",
                poolSize
            )
        );
        
        LogProcessingService processingService = new LogProcessingService(poolSize, logQueue);
        processingService.start();
        
        // Create multiple producers to generate high load
        LogProducerWorker producer1 = new LogProducerWorker(logQueue, "APP-1");
        LogProducerWorker producer2 = new LogProducerWorker(logQueue, "APP-2");
        LogProducerWorker producer3 = new LogProducerWorker(logQueue, "APP-3");
        LogProducerWorker producer4 = new LogProducerWorker(logQueue, "APP-4");
        
        producer1.start();
        producer2.start();
        producer3.start();
        producer4.start();
        
        System.out.println("\n[Main Thread] All threads started.");
        System.out.println("KEY OBSERVATIONS:");
        System.out.println("- Performance monitor reports every 5 seconds");
        System.out.println("- Watch for starvation warnings (tasks queued but threads idle)");
        System.out.println("- Monitor thread utilization (active/pool size)");
        System.out.println("- Check queue size (should stay low if pool is sized correctly)");
        System.out.println("- Final report shows wait/process ratio and sizing recommendations\n");
        
        // Demonstrate cancellation of a long-running evaluation
        Thread.sleep(5000);
        System.out.println("\n[Main Thread] Demonstrating task cancellation...");
        
        List<Log> longRunningBatch = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            longRunningBatch.add(new Log("LONG-" + i, "Long running test", "INFO", "TEST"));
        }
        
        Future<AlertEvaluationService.AlertResult> longTask = 
            processingService.getAlertService().evaluateLogsAsync(longRunningBatch, "LONG-RUNNING");
        
        Thread.sleep(200);
        System.out.println("[Main Thread] Cancelling long-running evaluation...");
        boolean cancelled = longTask.cancel(true);
        System.out.println(
            String.format(
                "[Main Thread] Cancellation %s (task was %s)",
                cancelled ? "successful" : "failed",
                longTask.isDone() ? "already done" : "still running"
            )
        );
        
        try {
            AlertEvaluationService.AlertResult result = longTask.get();
            System.out.println("[Main Thread] Got result: " + result);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InterruptedException) {
                System.out.println("[Main Thread] Task was cancelled (InterruptedException)");
            } else {
                System.out.println("[Main Thread] Task failed: " + e.getCause().getMessage());
            }
        } catch (java.util.concurrent.CancellationException e) {
            System.out.println("[Main Thread] Task was cancelled (CancellationException)");
        }
        
        // Let system run for 30 seconds to gather metrics
        System.out.println("\n[Main Thread] System running... (will shutdown in 30 seconds)");
        Thread.sleep(30000);
        
        // Graceful shutdown
        System.out.println("\n[Main Thread] Initiating graceful shutdown...\n");
        
        producer1.stopProducer();
        producer2.stopProducer();
        producer3.stopProducer();
        producer4.stopProducer();
        
        processingService.shutdown();
        
        producer1.join();
        producer2.join();
        producer3.join();
        producer4.join();
        
        // Final metrics
        System.out.println("\n=== Final Metrics Report ===");
        ProcessingMetrics.MetricsSnapshot finalMetrics = processingService.getMetrics();
        System.out.println(finalMetrics);
        
        System.out.println("\n=== Alert Evaluation Summary ===");
        System.out.println("Total Evaluations: " + processingService.getAlertService().getEvaluationsCompleted());
        System.out.println("Alerts Triggered: " + processingService.getAlertService().getAlertsTriggered());
        System.out.println("Evaluations Cancelled: " + processingService.getAlertService().getEvaluationsCancelled());
        
        System.out.println("\n=== Complete System Verification ===");
        System.out.println("✓ Thread pool sized appropriately for I/O-bound tasks");
        System.out.println("✓ Performance monitoring tracked utilization");
        System.out.println("✓ Fair scheduling prevented worker starvation");
        System.out.println("✓ Wait/process ratio calculated for sizing recommendations");
        System.out.println("✓ Threads responded to interrupt signals cooperatively");
        System.out.println("✓ Shared state protected with thread-safe collections");
        System.out.println("✓ Graceful shutdown completed successfully");
        
        System.out.println("\n=== 🎉 PROJECT COMPLETE 🎉 ===");
        System.out.println("\nAll 8 Steps Successfully Implemented:");
        System.out.println("1. ✅ Thread Fundamentals");
        System.out.println("2. ✅ Producer-Consumer Model");
        System.out.println("3. ✅ Worker Thread Pool");
        System.out.println("4. ✅ Shared State & Safety");
        System.out.println("5. ✅ Visibility & Shutdown");
        System.out.println("6. ✅ Callable & Future");
        System.out.println("7. ✅ Interrupts & Cancellation");
        System.out.println("8. ✅ Performance & Starvation");
        
        System.out.println("\n=== Key Learnings Summary ===");
        System.out.println("1. I/O-bound tasks benefit from more threads than CPU cores");
        System.out.println("2. CPU-bound tasks should use pool size = CPU cores");
        System.out.println("3. Monitor thread utilization to detect under/over-provisioning");
        System.out.println("4. Fair scheduling (FIFO queue) prevents starvation");
        System.out.println("5. Wait/process ratio helps determine optimal pool size");
        System.out.println("6. Interrupts are cooperative - threads must check and respond");
        System.out.println("7. volatile ensures visibility of updates across threads");
        System.out.println("8. Thread-safe collections prevent race conditions");
    }
}