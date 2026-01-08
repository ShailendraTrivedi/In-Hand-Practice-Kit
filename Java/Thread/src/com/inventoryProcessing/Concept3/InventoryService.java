package com.inventoryProcessing.Concept3;

import java.util.HashMap;
import java.util.Map;

/**
 * CONCEPT #2 - AFTER: InventoryService WITH synchronization
 * 
 * ✅ THIS VERSION IS THREAD-SAFE ✅
 * 
 * Definition:
 * - synchronized block: Ensures only one thread can execute the block at a time
 * - Mutual exclusion: Provides exclusive access to shared resources
 * - Monitor lock: Each object has an intrinsic lock (monitor) that synchronized uses
 * 
 * Why needed in e-commerce:
 * - Prevents overselling (negative inventory)
 * - Ensures accurate stock counts
 * - Prevents lost updates when multiple orders process simultaneously
 * 
 * Safety Measure:
 * - synchronized blocks: Protect critical sections that modify shared state
 * - Using 'this' as the lock object (can also use a dedicated lock object)
 * - All methods that access/modify inventory are synchronized
 */
public class InventoryService {
    
    // Shared state: Product ID -> Stock quantity
    // ✅ Protected by synchronized blocks
    private final Map<String, Integer> inventory = new HashMap<>();
    
    /**
     * Initialize inventory with some products.
     */
    public InventoryService() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }
    
    /**
     * Check if product has enough stock.
     * ✅ SYNCHRONIZED: Prevents other threads from modifying inventory during check
     */
    public synchronized boolean hasStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        if (currentStock == null) {
            return false;
        }
        return currentStock >= quantity;
    }
    
    /**
     * Reserve inventory (decrease stock) - ATOMIC OPERATION.
     * ✅ SYNCHRONIZED: Ensures read-modify-write is atomic
     * 
     * How synchronized works:
     * 1. Thread enters synchronized block → acquires lock on 'this' object
     * 2. Other threads trying to enter wait (blocked)
     * 3. Thread completes → releases lock
     * 4. Next waiting thread acquires lock and proceeds
     * 
     * This ensures:
     * - Only one thread can modify inventory at a time
     * - Read-modify-write operations are atomic
     * - No lost updates
     */
    public synchronized boolean reserveStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        
        if (currentStock == null || currentStock < quantity) {
            return false; // Not enough stock
        }
        
        // ✅ This entire block is atomic - no other thread can interfere
        // Simulate some processing time (safe now because we have the lock)
        try {
            Thread.sleep(1); // Simulate database/network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        int newStock = currentStock - quantity;
        inventory.put(productId, newStock);
        
        return true;
    }
    
    /**
     * Get current stock.
     * ✅ SYNCHRONIZED: Ensures we read consistent data
     */
    public synchronized int getStock(String productId) {
        Integer stock = inventory.get(productId);
        return stock == null ? 0 : stock;
    }
    
    /**
     * Get all inventory (for display).
     * ✅ SYNCHRONIZED: Prevents modification during copy
     */
    public synchronized Map<String, Integer> getAllInventory() {
        return new HashMap<>(inventory); // Return copy to prevent external modification
    }
    
    /**
     * Alternative: Using synchronized block instead of synchronized method
     * This gives more control over what is synchronized.
     * 
     * Example (equivalent to synchronized method):
     * public boolean reserveStock(String productId, int quantity) {
     *     synchronized(this) {
     *         // critical section
     *     }
     * }
     * 
     * Interview Tip:
     * - synchronized method = synchronized(this) { ... }
     * - Can synchronize on any object, not just 'this'
     * - Best practice: Use dedicated lock objects for better control
     */
}