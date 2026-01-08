package com.inventoryProcessing.Concept4;

/**
 * CONCEPT #4 DEMONSTRATION: wait() / notifyAll (Producer–Consumer queue)
 * 
 * This demo shows:
 * 1. BEFORE: Busy-waiting (inefficient CPU usage)
 * 2. AFTER: wait/notify (efficient thread coordination)
 * 
 * Interview Tip:
 * - wait() must be in synchronized block
 * - Always use while loop with wait() (spurious wakeups)
 * - notifyAll() is safer than notify() for multiple consumers
 * - wait() releases lock atomically
 */
public class OrderProcessingDemo4 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONCEPT #4: wait() / notifyAll (Producer–Consumer Queue) ===\n");
        
        // ============================================
        // PART 1: Demonstrating busy-waiting problem
        // ============================================
        System.out.println("--- PART 1: Busy-Waiting Problem (Inefficient) ---");
        System.out.println("⚠️  WARNING: This will use 100% CPU when queue is empty!\n");
        System.out.println("(Skipping actual busy-waiting demo to avoid CPU spike)");
        System.out.println("In real scenario, consumer threads would continuously poll the queue");
        System.out.println("wasting CPU cycles even when there's no work.\n");
        
        Thread.sleep(1000);
        
        // ============================================
        // PART 2: Solution with wait/notify
        // ============================================
        System.out.println("--- PART 2: Solution with wait/notify (Efficient) ---\n");
        demonstrateWaitNotifySolution();
    }
    
    /**
     * Demonstrates efficient producer-consumer pattern using wait/notify.
     */
    private static void demonstrateWaitNotifySolution() throws InterruptedException {
        System.out.println("Setting up Producer-Consumer system...");
        System.out.println("  - Queue size: 10");
        System.out.println("  - Producers: 2");
        System.out.println("  - Consumers: 3");
        System.out.println("  - Orders per producer: 5\n");
        
        InventoryService inventoryService = new InventoryService();
        OrderQueue queue = new OrderQueue(10); // Max 10 orders
        
        // Create producers
        OrderProducer[] producers = {
            new OrderProducer(queue, 1, 5),
            new OrderProducer(queue, 2, 5)
        };
        
        // Create consumers
        OrderConsumer[] consumers = {
            new OrderConsumer(queue, 1, inventoryService),
            new OrderConsumer(queue, 2, inventoryService),
            new OrderConsumer(queue, 3, inventoryService)
        };
        
        // Start consumer threads
        Thread[] consumerThreads = new Thread[consumers.length];
        for (int i = 0; i < consumers.length; i++) {
            consumerThreads[i] = new Thread(consumers[i], "Consumer-" + (i + 1));
            consumerThreads[i].start();
        }
        
        // Start producer threads
        Thread[] producerThreads = new Thread[producers.length];
        for (int i = 0; i < producers.length; i++) {
            producerThreads[i] = new Thread(producers[i], "Producer-" + (i + 1));
            producerThreads[i].start();
        }
        
        // Wait for all producers to finish
        for (Thread thread : producerThreads) {
            thread.join();
        }
        
        System.out.println("\n[System] All producers finished. Waiting for consumers to process remaining orders...");
        
        // Wait a bit for consumers to process
        Thread.sleep(2000);
        
        // Shutdown queue (signals consumers to stop)
        System.out.println("\n[System] Shutting down queue...");
        queue.shutdown();
        
        // Wait for consumers to finish
        for (Thread thread : consumerThreads) {
            thread.join(1000); // Wait max 1 second
        }
        
        // Print statistics
        System.out.println("\n=== Final Statistics ===");
        System.out.println("Queue size: " + queue.size());
        System.out.println("\nProducers:");
        for (OrderProducer producer : producers) {
            System.out.println("  Producer-" + producer.getProducerId() + ": " + 
                             producer.getOrdersProduced() + " orders produced");
        }
        System.out.println("\nConsumers:");
        for (OrderConsumer consumer : consumers) {
            System.out.println("  Consumer-" + consumer.getConsumerId() + ": " + 
                             consumer.getOrdersProcessed() + " orders processed");
        }
        
        System.out.println("\n=== Key Takeaways ===");
        System.out.println("1. wait() makes thread sleep (no CPU usage)");
        System.out.println("2. notifyAll() wakes up waiting threads");
        System.out.println("3. wait() releases lock, allowing other threads to proceed");
        System.out.println("4. Always use while loop with wait() (handles spurious wakeups)");
        System.out.println("5. Much more efficient than busy-waiting");
        
        System.out.println("\n=== How wait/notify Works ===");
        System.out.println("Producer (when queue full):");
        System.out.println("  1. Acquires lock (synchronized)");
        System.out.println("  2. Checks queue.size() >= maxSize");
        System.out.println("  3. Calls wait() → releases lock, goes to sleep");
        System.out.println("  4. Consumer processes order, calls notifyAll()");
        System.out.println("  5. Producer wakes up, re-acquires lock, adds order");
        
        System.out.println("\nConsumer (when queue empty):");
        System.out.println("  1. Acquires lock (synchronized)");
        System.out.println("  2. Checks queue.isEmpty()");
        System.out.println("  3. Calls wait() → releases lock, goes to sleep");
        System.out.println("  4. Producer adds order, calls notifyAll()");
        System.out.println("  5. Consumer wakes up, re-acquires lock, removes order");
        
        System.out.println("\n=== Important Rules ===");
        System.out.println("✓ wait() and notifyAll() must be in synchronized block");
        System.out.println("✓ Always use while loop with wait() (not if)");
        System.out.println("✓ Use notifyAll() instead of notify() for multiple consumers");
        System.out.println("✓ wait() releases lock atomically");
    }
}