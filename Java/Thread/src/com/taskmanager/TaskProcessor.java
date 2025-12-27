package com.taskmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Task processor using ExecutorService.
 * Demonstrates: ExecutorService, Callable, Future, proper shutdown
 */
public class TaskProcessor {
    private final ExecutorService executorService;
    private final List<Future<TaskResult>> futures;

    /**
     * Concept 7: ExecutorService with thread pool
     */
    public TaskProcessor(int poolSize) {
        // Fixed thread pool - prevents thread explosion
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.futures = new ArrayList<>();
    }

    /**
     * Concept 8: Callable & Future for returning results
     */
    public Future<TaskResult> submitTask(Task task) {
        Callable<TaskResult> taskCallable = () -> {
            System.out.println("Processing task " + task.getId() + " in thread " +
                    Thread.currentThread().getName());

            task.setStatus(TaskStatus.PROCESSING);

            // Simulate work
            Thread.sleep((long) (Math.random() * 2000));

            // Check for cancellation
            if (Thread.currentThread().isInterrupted() || task.isCancelled()) {
                task.setStatus(TaskStatus.CANCELLED);
                return new TaskResult(task, false, "Cancelled");
            }

            // Simulate occasional failure
            if (Math.random() < 0.15) {
                task.setStatus(TaskStatus.FAILED);
                return new TaskResult(task, false, "Processing failed");
            }

            task.setStatus(TaskStatus.COMPLETED);
            return new TaskResult(task, true, "Success");
        };

        Future<TaskResult> future = executorService.submit(taskCallable);
        futures.add(future);
        return future;
    }

    /**
     * Wait for all tasks to complete
     */
    public List<TaskResult> waitForAll() throws InterruptedException, ExecutionException {
        List<TaskResult> results = new ArrayList<>();

        for (Future<TaskResult> future : futures) {
            try {
                // Concept 8: Future.get() blocks until result is available
                TaskResult result = future.get();
                results.add(result);
            } catch (ExecutionException e) {
                System.err.println("Task execution exception: " + e.getCause().getMessage());
            }
        }

        return results;
    }

    /**
     * Concept 11: Proper shutdown handling
     */
    public void shutdown() {
        System.out.println("Shutting down TaskProcessor...");
        executorService.shutdown();

        try {
            // Wait for running tasks to complete
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown...");
                executorService.shutdownNow(); // Cancel running tasks

                // Wait again
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("TaskProcessor shut down complete");
    }

    public static class TaskResult {
        private final Task task;
        private final boolean success;
        private final String message;

        public TaskResult(Task task, boolean success, String message) {
            this.task = task;
            this.success = success;
            this.message = message;
        }

        public Task getTask() {
            return task;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
