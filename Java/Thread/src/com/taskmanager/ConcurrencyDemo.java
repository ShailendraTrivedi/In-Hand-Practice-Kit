package com.taskmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Main demonstration class showing all concurrency concepts in action.
 * Simulates heavy load with hundreds of tasks.
 */
public class ConcurrencyDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Multi-Threaded Task Management System ===\n");
        
        // Demonstrate all concepts
        demonstrateRaceCondition();
        demonstrateProducerConsumer();
        demonstrateExecutorService();
        demonstrateDeadlock();
        demonstrateInterrupts();
        demonstrateShutdown();
        
        System.out.println("\n=== All demonstrations complete ===");
    }
    
    /**
     * Concept 2: Race Condition demonstration
     */
    private static void demonstrateRaceCondition() throws InterruptedException {
        System.out.println("\n--- DEMO 1: Race Condition (Bug + Fix) ---");
        
        TaskManager manager = new TaskManager();
        int numThreads = 10;
        int incrementsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        // BUGGY VERSION
        System.out.println("Running BUGGY version (race condition)...");
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    manager.incrementCompletedBuggy();
                }
                latch.countDown();
            }).start();
        }
        
        latch.await();
        System.out.println("Expected: " + (numThreads * incrementsPerThread));
        System.out.println("Actual (buggy): " + manager.getCompletedCountBuggy());
        System.out.println("Lost updates: " + 
            (numThreads * incrementsPerThread - manager.getCompletedCountBuggy()));
        
        // FIXED VERSION
        System.out.println("\nRunning FIXED version (synchronized)...");
        CountDownLatch latch2 = new CountDownLatch(numThreads);
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    manager.incrementCompletedFixed();
                }
                latch2.countDown();
            }).start();
        }
        
        latch2.await();
        System.out.println("Expected: " + (numThreads * incrementsPerThread));
        System.out.println("Actual (fixed): " + manager.getCompletedCountFixed());
    }
    
    /**
     * Concept 5: Producer-Consumer with wait/notify
     */
    private static void demonstrateProducerConsumer() throws InterruptedException {
        System.out.println("\n--- DEMO 2: Producer-Consumer Pattern ---");
        
        TaskQueue queue = new TaskQueue(10);
        List<Worker> workers = new ArrayList<>();
        List<Thread> workerThreads = new ArrayList<>();
        
        // Start consumer threads (Concept 1: Thread & Runnable)
        int numWorkers = 3;
        for (int i = 0; i < numWorkers; i++) {
            Worker worker = new Worker(i, queue);
            workers.add(worker);
            Thread t = new Thread(worker);
            workerThreads.add(t);
            t.start();
        }
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    Task task = new Task("Task " + i);
                    queue.enqueue(task);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        producer.join();
        
        // Wait for queue to empty
        while (!queue.isEmpty()) {
            Thread.sleep(100);
        }
        
        // Stop workers
        for (Worker worker : workers) {
            worker.stop();
        }
        
        for (Thread t : workerThreads) {
            t.interrupt();
            t.join();
        }
    }
    
    /**
     * Concept 7 & 8: ExecutorService, Callable, Future
     */
    private static void demonstrateExecutorService() throws InterruptedException {
        System.out.println("\n--- DEMO 3: ExecutorService & Future ---");
        
        TaskProcessor processor = new TaskProcessor(5);
        List<Task> tasks = new ArrayList<>();
        
        // Submit 50 tasks
        for (int i = 0; i < 50; i++) {
            Task task = new Task("ExecutorTask " + i);
            tasks.add(task);
            processor.submitTask(task);
        }
        
        // Wait for all to complete
        try {
            List<TaskProcessor.TaskResult> results = processor.waitForAll();
            
            long successCount = results.stream()
                .filter(TaskProcessor.TaskResult::isSuccess)
                .count();
            
            System.out.println("Total tasks: " + results.size());
            System.out.println("Successful: " + successCount);
            System.out.println("Failed: " + (results.size() - successCount));
            
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            processor.shutdown();
        }
    }
    
    /**
     * Concept 6: Deadlock demonstration
     */
    private static void demonstrateDeadlock() throws InterruptedException {
        System.out.println("\n--- DEMO 4: Deadlock (Bug + Fix) ---");
        
        TaskScheduler scheduler = new TaskScheduler();
        
        // DEADLOCK VERSION (commented out - will hang!)
        /*
        System.out.println("Running DEADLOCK version (will hang)...");
        Thread t1 = new Thread(() -> scheduler.scheduleTaskDeadlock(new Task("Task1"), true));
        Thread t2 = new Thread(() -> scheduler.scheduleTaskDeadlock(new Task("Task2"), false));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        */
        
        // FIXED VERSION
        System.out.println("Running FIXED version (no deadlock)...");
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                scheduler.scheduleTaskFixed(new Task("FixedTask " + taskId));
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("All tasks scheduled without deadlock!");
    }
    
    /**
     * Concept 9: Interrupts for graceful cancellation
     */
    private static void demonstrateInterrupts() throws InterruptedException {
        System.out.println("\n--- DEMO 5: Interrupts & Cancellation ---");
        
        TaskQueue queue = new TaskQueue(5);
        Worker worker = new Worker(999, queue);
        Thread workerThread = new Thread(worker);
        workerThread.start();
        
        // Add a task
        queue.enqueue(new Task("InterruptTest"));
        
        Thread.sleep(500);
        
        // Interrupt the worker
        System.out.println("Interrupting worker thread...");
        workerThread.interrupt();
        workerThread.join();
        
        System.out.println("Worker thread stopped gracefully");
    }
    
    /**
     * Concept 11: Proper shutdown handling
     */
    private static void demonstrateShutdown() throws InterruptedException {
        System.out.println("\n--- DEMO 6: Proper Shutdown Handling ---");
        
        TaskProcessor processor = new TaskProcessor(3);
        
        // Submit many tasks
        for (int i = 0; i < 100; i++) {
            processor.submitTask(new Task("ShutdownTest " + i));
        }
        
        System.out.println("Submitted 100 tasks, shutting down gracefully...");
        
        // Proper shutdown
        processor.shutdown();
        
        System.out.println("Shutdown complete");
    }
    
    /**
     * Concept 10: Thread starvation & performance tuning
     * This would be demonstrated by monitoring thread utilization
     * and adjusting pool sizes based on workload characteristics.
     */
    private static void demonstratePerformanceTuning() {
        System.out.println("\n--- DEMO 7: Performance Tuning ---");
        System.out.println("Key principles:");
        System.out.println("1. Use appropriate thread pool size (CPU cores * 2 for I/O bound)");
        System.out.println("2. Use work-stealing pools for CPU-bound tasks");
        System.out.println("3. Monitor thread utilization and adjust dynamically");
        System.out.println("4. Use thread-safe collections (ConcurrentHashMap, etc.)");
        System.out.println("5. Minimize lock contention (fine-grained locking)");
    }
}