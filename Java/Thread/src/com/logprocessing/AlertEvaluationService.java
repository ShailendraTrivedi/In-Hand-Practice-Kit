package com.logprocessing;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * STEP 7: Enhanced Alert Evaluation Service with Cancellation Support
 * 
 * Demonstrates proper interrupt handling in Callable tasks.
 */
public class AlertEvaluationService {
    
    private final ExecutorService evaluationExecutor;
    private final ProcessingMetrics metrics;
    
    private final AtomicInteger alertsTriggered = new AtomicInteger(0);
    private final AtomicInteger evaluationsCompleted = new AtomicInteger(0);
    private final AtomicInteger evaluationsCancelled = new AtomicInteger(0);
    
    public AlertEvaluationService(ProcessingMetrics metrics) {
        this.evaluationExecutor = Executors.newFixedThreadPool(3);
        this.metrics = metrics;
        System.out.println("[AlertEvaluationService] Created evaluation thread pool");
    }
    
    /**
     * Evaluates logs with proper interrupt handling.
     * 
     * This Callable checks for interruption and responds appropriately.
     */
    public Future<AlertResult> evaluateLogsAsync(List<Log> logs, String evaluationId) {
        Future<AlertResult> future = evaluationExecutor.submit(new Callable<AlertResult>() {
            @Override
            public AlertResult call() throws Exception {
                System.out.println(
                    String.format(
                        "[Alert Evaluator] Starting evaluation %s for %d logs | Thread: %s",
                        evaluationId,
                        logs.size(),
                        Thread.currentThread().getName()
                    )
                );
                
                // Check interrupt status before starting
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println(
                        String.format(
                            "[Alert Evaluator] Already interrupted before starting: %s",
                            evaluationId
                        )
                    );
                    evaluationsCancelled.incrementAndGet();
                    throw new InterruptedException("Evaluation cancelled before start");
                }
                
                // Simulate work with interrupt checking
                int steps = 10;
                for (int i = 0; i < steps; i++) {
                    // COOPERATIVE CANCELLATION: Check interrupt status
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println(
                            String.format(
                                "[Alert Evaluator] Interrupted during evaluation %s (step %d/%d)",
                                evaluationId,
                                i + 1,
                                steps
                            )
                        );
                        evaluationsCancelled.incrementAndGet();
                        // Restore interrupt status
                        Thread.currentThread().interrupt();
                        throw new InterruptedException("Evaluation cancelled");
                    }
                    
                    // Simulate work
                    Thread.sleep(50 + (int)(Math.random() * 50)); // 50-100ms per step
                }
                
                // Final check before returning result
                if (Thread.currentThread().isInterrupted()) {
                    evaluationsCancelled.incrementAndGet();
                    Thread.currentThread().interrupt();
                    throw new InterruptedException("Evaluation cancelled before completion");
                }
                
                AlertResult result = evaluateLogs(logs);
                evaluationsCompleted.incrementAndGet();
                
                System.out.println(
                    String.format(
                        "[Alert Evaluator] Completed evaluation %s | Result: %s | Thread: %s",
                        evaluationId,
                        result.shouldTriggerAlert() ? "ALERT" : "OK",
                        Thread.currentThread().getName()
                    )
                );
                
                return result;
            }
        });
        
        return future;
    }
    
    private AlertResult evaluateLogs(List<Log> logs) {
        int errorCount = 0;
        int warningCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        for (Log log : logs) {
            if ("ERROR".equals(log.getLevel())) {
                errorCount++;
                errorMessages.add(log.getMessage());
            } else if ("WARNING".equals(log.getLevel())) {
                warningCount++;
            }
        }
        
        double errorRate = logs.size() > 0 ? (double) errorCount / logs.size() * 100 : 0;
        
        boolean shouldAlert = false;
        String reason = "";
        
        if (errorRate > 10.0) {
            shouldAlert = true;
            reason = String.format("Error rate %.2f%% exceeds threshold (10%%)", errorRate);
        } else if (errorCount > 5) {
            shouldAlert = true;
            reason = String.format("Error count %d exceeds threshold (5)", errorCount);
        } else if (errorCount > 0 && warningCount > 10) {
            shouldAlert = true;
            reason = String.format("High warning count (%d) with errors present", warningCount);
        }
        
        if (shouldAlert) {
            alertsTriggered.incrementAndGet();
        }
        
        return new AlertResult(shouldAlert, reason, errorCount, warningCount, errorRate, errorMessages);
    }
    
    public List<AlertResult> evaluateMultipleBatches(List<List<Log>> logBatches) 
            throws InterruptedException, ExecutionException {
        
        System.out.println(
            String.format(
                "[AlertEvaluationService] Evaluating %d batches concurrently...",
                logBatches.size()
            )
        );
        
        List<Future<AlertResult>> futures = new ArrayList<>();
        for (int i = 0; i < logBatches.size(); i++) {
            Future<AlertResult> future = evaluateLogsAsync(logBatches.get(i), "BATCH-" + (i + 1));
            futures.add(future);
        }
        
        List<AlertResult> results = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            Future<AlertResult> future = futures.get(i);
            try {
                AlertResult result = future.get(5, TimeUnit.SECONDS);
                results.add(result);
            } catch (TimeoutException e) {
                System.err.println(
                    String.format(
                        "[AlertEvaluationService] Evaluation BATCH-%d timed out, cancelling...",
                        i + 1
                    )
                );
                // Cancel the task (sends interrupt)
                boolean cancelled = future.cancel(true);
                System.out.println(
                    String.format(
                        "[AlertEvaluationService] Cancellation %s for BATCH-%d",
                        cancelled ? "successful" : "failed",
                        i + 1
                    )
                );
                results.add(new AlertResult(false, "Evaluation timed out and cancelled", 0, 0, 0, new ArrayList<>()));
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InterruptedException) {
                    System.out.println(
                        String.format(
                            "[AlertEvaluationService] Evaluation BATCH-%d was cancelled",
                            i + 1
                        )
                    );
                } else {
                    System.err.println(
                        String.format(
                            "[AlertEvaluationService] Evaluation BATCH-%d failed: %s",
                            i + 1,
                            e.getCause().getMessage()
                        )
                    );
                }
                results.add(new AlertResult(false, "Evaluation failed: " + e.getCause().getMessage(), 
                                           0, 0, 0, new ArrayList<>()));
            }
        }
        
        return results;
    }
    
    public void shutdown() throws InterruptedException {
        System.out.println("[AlertEvaluationService] Shutting down...");
        evaluationExecutor.shutdown();
        
        if (!evaluationExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
            System.out.println("[AlertEvaluationService] Forcing shutdown (interrupting tasks)...");
            evaluationExecutor.shutdownNow();
        }
        
        System.out.println(
            String.format(
                "[AlertEvaluationService] Shutdown complete. Evaluations: %d, Alerts: %d, Cancelled: %d",
                evaluationsCompleted.get(),
                alertsTriggered.get(),
                evaluationsCancelled.get()
            )
        );
    }
    
    public int getAlertsTriggered() {
        return alertsTriggered.get();
    }
    
    public int getEvaluationsCompleted() {
        return evaluationsCompleted.get();
    }
    
    public int getEvaluationsCancelled() {
        return evaluationsCancelled.get();
    }
    
    public static class AlertResult {
        private final boolean shouldTriggerAlert;
        private final String reason;
        private final int errorCount;
        private final int warningCount;
        private final double errorRate;
        private final List<String> errorMessages;
        
        public AlertResult(boolean shouldTriggerAlert, String reason, 
                          int errorCount, int warningCount, double errorRate,
                          List<String> errorMessages) {
            this.shouldTriggerAlert = shouldTriggerAlert;
            this.reason = reason;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.errorRate = errorRate;
            this.errorMessages = errorMessages;
        }
        
        public boolean shouldTriggerAlert() { return shouldTriggerAlert; }
        public String getReason() { return reason; }
        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
        public double getErrorRate() { return errorRate; }
        public List<String> getErrorMessages() { return errorMessages; }
        
        @Override
        public String toString() {
            if (shouldTriggerAlert) {
                return String.format("ALERT: %s (Errors: %d, Warnings: %d, Rate: %.2f%%)",
                    reason, errorCount, warningCount, errorRate);
            } else {
                return String.format("OK (Errors: %d, Warnings: %d, Rate: %.2f%%)",
                    errorCount, warningCount, errorRate);
            }
        }
    }
}