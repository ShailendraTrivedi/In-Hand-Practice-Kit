package com.inventoryProcessing.Concept8;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * CONCEPT #8 DEMONSTRATION: Thread-safe Collections
 * 
 * This demo shows:
 * 1. BEFORE: Synchronized collections (Collections.synchronizedMap, etc.)
 * 2. AFTER: Thread-safe collections (ConcurrentHashMap, BlockingQueue, etc.)
 * 
 * Interview Tip:
 * - ConcurrentHashMap: Better for high concurrency
 * - BlockingQueue: Perfect for producer-consumer
 * - CopyOnWriteArrayList: Good for read-heavy, write-rarely scenarios
 * - Choose based on access patterns (read-heavy vs write-heavy)
 */
public class OrderProcessingDemo8 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #8: Thread-safe Collections ===\n");
        
        // ============================================
        // PART 1: Synchronized collections (BEFORE)
        // ============================================
        System.out.println("--- PART 1: Synchronized Collections (BEFORE) ---\n");
        demonstrateSynchronizedCollections();
        
        Thread.sleep(2000);
        
        // ============================================
        // PART 2: Thread-safe collections (AFTER)
        // ============================================
        System.out.println("\n\n--- PART 2: Thread-safe Collections (AFTER) ---\n");
        demonstrateThreadSafeCollections();
        
        // ============================================
        // PART 3: BlockingQueue demonstration
        // ============================================
        System.out.println("\n\n--- PART 3: BlockingQueue (Producer-Consumer) ---\n");
        demonstrateBlockingQueue();
    }
    
    /**
     * Demonstrates synchronized collections.
     * ⚠️  Less efficient under high concurrency
     */
    private static void demonstrateSynchronizedCollections() throws InterruptedException {
        System.out.println("Using Collections.synchronizedMap()...");
        System.out.println("⚠️  Coarse-grained locking (entire map locked)\n");
        
        InventoryServiceSynchronized service = new InventoryServiceSynchronized();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numTasks = 100;
        CountDownLatch latch = new CountDownLatch(numTasks);
        
        long startTime = System.currentTimeMillis();
        
        // Submit many concurrent tasks
        for (int i = 0; i < numTasks; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    String productId = "PROD-00" + ((taskId % 5) + 1);
                    service.reserveStock(productId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("--- Results (Synchronized Collections) ---");
        System.out.println("Tasks completed: " + numTasks);
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("⚠️  All operations lock entire map");
        System.out.println("⚠️  Lower concurrency (only one thread at a time)");
    }
    
    /**
     * Demonstrates thread-safe collections.
     * ✅ More efficient under high concurrency
     */
    private static void demonstrateThreadSafeCollections() throws InterruptedException {
        System.out.println("Using ConcurrentHashMap...");
        System.out.println("✅ Fine-grained locking (only segments locked)\n");
        
        InventoryService service = new InventoryService();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numTasks = 100;
        CountDownLatch latch = new CountDownLatch(numTasks);
        
        long startTime = System.currentTimeMillis();
        
        // Submit many concurrent tasks
        for (int i = 0; i < numTasks; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    String productId = "PROD-00" + ((taskId % 5) + 1);
                    service.reserveStock(productId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("--- Results (Thread-safe Collections) ---");
        System.out.println("Tasks completed: " + numTasks);
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("✅ Fine-grained locking (only segments locked)");
        System.out.println("✅ Higher concurrency (multiple threads can access)");
        System.out.println("✅ Concurrent reads don't block each other");
        
        // Show inventory status
        System.out.println("\n=== Final Inventory Status ===");
        System.out.println("PROD-001: " + service.getStock("PROD-001"));
        System.out.println("PROD-002: " + service.getStock("PROD-002"));
        System.out.println("PROD-003: " + service.getStock("PROD-003"));
        System.out.println("PROD-004: " + service.getStock("PROD-004"));
        System.out.println("PROD-005: " + service.getStock("PROD-005"));
    }
    
    /**
     * Demonstrates BlockingQueue for producer-consumer pattern.
     */
    private static void demonstrateBlockingQueue() throws InterruptedException {
        System.out.println("Demonstrating BlockingQueue (Producer-Consumer)...");
        System.out.println("✅ Built-in blocking operations (no manual wait/notify)\n");
        
        OrderQueue queue = new OrderQueue(10);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        // Create producers
        List<Future<?>> producerFutures = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            final int producerId = i;
            Future<?> future = executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        Order order = new Order("ORD-" + String.format("%03d", producerId * 10 + j),
                                              "USER-" + (100 + j),
                                              "PROD-00" + ((j % 5) + 1),
                                              1,
                                              99.99);
                        queue.enqueue(order);
                        Thread.sleep(50);
                    }
                    System.out.println("[Producer-" + producerId + "] Finished");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            producerFutures.add(future);
        }
        
        // Create consumers
        List<Future<?>> consumerFutures = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final int consumerId = i;
            Future<?> future = executor.submit(() -> {
                int processed = 0;
                try {
                    while (true) {
                        Order order = queue.dequeue();
                        if (order == null || "SHUTDOWN".equals(order.getOrderId())) {
                            break;
                        }
                        System.out.println("[Consumer-" + consumerId + "] Processing: " + order.getOrderId());
                        processed++;
                        Thread.sleep(100); // Simulate processing
                    }
                    System.out.println("[Consumer-" + consumerId + "] Finished (processed: " + processed + ")");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            consumerFutures.add(future);
        }
        
        // Wait for producers to finish
        for (Future<?> future : producerFutures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                System.err.println("Producer error: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\n[System] All producers finished. Shutting down queue...");
        queue.shutdown();
        
        // Wait for consumers to finish
        for (Future<?> future : consumerFutures) {
            try {
                future.get(2, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                System.err.println("Consumer error: " + e.getCause().getMessage());
            } catch (TimeoutException e) {
                System.err.println("Consumer timeout");
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\n=== Key Benefits of BlockingQueue ===");
        System.out.println("1. ✅ Built-in blocking: put() and take() handle wait/notify");
        System.out.println("2. ✅ Thread-safe: No external synchronization needed");
        System.out.println("3. ✅ Cleaner code: No manual wait/notify");
        System.out.println("4. ✅ Better performance: Optimized for concurrent access");
        
        System.out.println("\n=== Thread-safe Collections Summary ===");
        System.out.println("ConcurrentHashMap:");
        System.out.println("  - Fine-grained locking (segments)");
        System.out.println("  - Concurrent reads");
        System.out.println("  - Atomic operations: compute(), merge()");
        System.out.println("  - Use for: High concurrency, read-heavy workloads");
        
        System.out.println("\nBlockingQueue:");
        System.out.println("  - Built-in blocking operations");
        System.out.println("  - Perfect for producer-consumer");
        System.out.println("  - Types: LinkedBlockingQueue, ArrayBlockingQueue");
        System.out.println("  - Use for: Producer-consumer patterns");
        
        System.out.println("\nCopyOnWriteArrayList:");
        System.out.println("  - Copy-on-write semantics");
        System.out.println("  - Safe iteration (snapshot)");
        System.out.println("  - Use for: Read-heavy, write-rarely scenarios");
        
        System.out.println("\nConcurrentLinkedQueue:");
        System.out.println("  - Lock-free implementation");
        System.out.println("  - Non-blocking operations");
        System.out.println("  - Use for: High-performance, non-blocking queues");
    }
}

