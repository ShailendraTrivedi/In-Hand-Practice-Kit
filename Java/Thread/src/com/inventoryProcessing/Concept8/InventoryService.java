package com.inventoryProcessing.Concept8;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CONCEPT #8 - AFTER: InventoryService using ConcurrentHashMap
 * 
 * ✅ THIS VERSION USES THREAD-SAFE COLLECTIONS (EFFICIENT) ✅
 * 
 * Definition:
 * - ConcurrentHashMap: Thread-safe HashMap with fine-grained locking
 * - Segment-based locking: Only locks portion of map being modified
 * - Concurrent reads: Multiple threads can read simultaneously
 * - Atomic operations: Methods like compute(), merge() for atomic updates
 * 
 * Why needed in e-commerce:
 * - High concurrency: Many threads accessing inventory simultaneously
 * - Better performance: Concurrent reads don't block each other
 * - Fine-grained locking: Only locks affected segments, not entire map
 * - Built-in atomic operations: No need for external synchronization
 * 
 * Safety Measure:
 * - ConcurrentHashMap: Thread-safe with better concurrency than synchronized map
 * - Fine-grained locking: Only locks segments, not entire map
 * - Concurrent reads: Multiple threads can read simultaneously
 * - Atomic operations: compute(), merge(), putIfAbsent() are atomic
 * 
 * Interview Tip:
 * - ConcurrentHashMap: Better for high concurrency (read-heavy workloads)
 * - Collections.synchronizedMap(): Simpler but less efficient
 * - Use ConcurrentHashMap when: High concurrency, many reads, some writes
 * - Use synchronized map when: Low concurrency, simple use case
 * - ConcurrentHashMap doesn't lock entire map (segment-based)
 */
public class InventoryService {
    
    // ✅ ConcurrentHashMap: Thread-safe with fine-grained locking
    // Multiple threads can read concurrently
    // Only locks segments being modified
    private final ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>();
    
    public InventoryService() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }
    
    /**
     * Check if product has enough stock.
     * ✅ Concurrent reads: Multiple threads can read simultaneously
     */
    public boolean hasStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        if (currentStock == null) {
            return false;
        }
        return currentStock >= quantity;
    }
    
    /**
     * Reserve inventory (decrease stock) - ATOMIC OPERATION.
     * ✅ Uses compute() for atomic read-modify-write
     * 
     * How ConcurrentHashMap works:
     * 1. Fine-grained locking: Only locks the segment containing the key
     * 2. Concurrent reads: Other segments can be read simultaneously
     * 3. Atomic operations: compute(), merge() are atomic
     * 4. No external synchronization needed
     */
    public boolean reserveStock(String productId, int quantity) {
        // ✅ Atomic operation: compute() is thread-safe
        // No need for external synchronization
        return inventory.compute(productId, (key, currentStock) -> {
            if (currentStock == null || currentStock < quantity) {
                return currentStock; // Return unchanged (operation failed)
            }
            
            // Simulate processing (within atomic operation)
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return currentStock; // Return unchanged on interrupt
            }
            
            // Return new value (operation succeeded)
            return currentStock - quantity;
        }) != null && inventory.get(productId) >= 0;
    }
    
    /**
     * Alternative: Using merge() for atomic updates
     * ✅ More concise for simple updates
     */
    public boolean reserveStockWithMerge(String productId, int quantity) {
        // ✅ merge() is atomic: updates value if condition is met
        Integer result = inventory.merge(productId, 0, (current, defaultValue) -> {
            if (current < quantity) {
                return current; // Not enough stock
            }
            return current - quantity; // Decrease stock
        });
        
        return result != null && result >= 0;
    }
    
    /**
     * Get current stock.
     * ✅ Concurrent read: No locking, multiple threads can read
     */
    public int getStock(String productId) {
        Integer stock = inventory.get(productId);
        return stock == null ? 0 : stock;
    }
    
    /**
     * Get all inventory.
     * ✅ Safe iteration: ConcurrentHashMap provides safe iteration
     */
    public Map<String, Integer> getAllInventory() {
        return new HashMap<>(inventory); // Safe to create copy
    }
    
    /**
     * Add stock (for restocking).
     * ✅ Atomic operation using compute()
     */
    public void addStock(String productId, int quantity) {
        inventory.compute(productId, (key, currentStock) -> {
            return (currentStock == null ? 0 : currentStock) + quantity;
        });
    }
}

