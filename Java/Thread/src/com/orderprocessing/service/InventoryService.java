package com.orderprocessing.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InventoryService manages product inventory in a thread-safe manner.
 * 
 * CONCURRENCY CONCEPT #2: synchronized blocks/methods
 * 
 * A. Definition:
 *    Synchronized blocks/methods ensure that only one thread can execute
 *    the critical section at a time, preventing race conditions.
 * 
 * B. Why It Is Needed in a Real Backend:
 *    Without synchronization, multiple threads could read and update
 *    inventory simultaneously, leading to overselling, negative stock,
 *    or incorrect inventory counts. This causes financial losses and
 *    customer dissatisfaction in production.
 * 
 * C. Safety Measure Used:
 *    Synchronized methods on checkInventory() and updateInventory()
 *    ensure atomic read-modify-write operations.
 * 
 * D. Safety Measure Definition:
 *    Synchronized methods acquire an intrinsic lock (monitor) on the
 *    object instance. Only one thread can hold this lock at a time,
 *    ensuring mutual exclusion. Other threads block until the lock
 *    is released, preventing concurrent modifications.
 */
public class InventoryService {
    // CONCURRENCY CONCEPT #8: Thread-safe collections
    // Using ConcurrentHashMap for thread-safe inventory storage
    // without explicit synchronization for read operations.
    private final Map<String, Integer> inventory;
    
    public InventoryService() {
        this.inventory = new ConcurrentHashMap<>();
        // Initialize with some products
        inventory.put("PROD-001", 1000);
        inventory.put("PROD-002", 500);
        inventory.put("PROD-003", 750);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 300);
    }
    
    /**
     * Checks if sufficient inventory is available.
     * Uses synchronized to prevent race conditions during inventory checks.
     */
    public synchronized boolean checkInventory(String productId, int quantity) {
        Integer available = inventory.get(productId);
        if (available == null) {
            return false;
        }
        return available >= quantity;
    }
    
    /**
     * Updates inventory after order processing.
     * Uses synchronized to ensure atomic inventory updates.
     */
    public synchronized boolean updateInventory(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        if (currentStock == null || currentStock < quantity) {
            return false;
        }
        inventory.put(productId, currentStock - quantity);
        return true;
    }
    
    /**
     * Restores inventory when order is cancelled.
     * Uses synchronized to ensure atomic inventory restoration.
     */
    public synchronized void restoreInventory(String productId, int quantity) {
        inventory.merge(productId, quantity, Integer::sum);
    }
    
    public synchronized int getStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }
}

