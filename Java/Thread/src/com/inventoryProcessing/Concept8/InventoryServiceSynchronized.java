package com.inventoryProcessing.Concept8;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * CONCEPT #8 - BEFORE: InventoryService using synchronized collections
 * 
 * ⚠️  THIS VERSION USES SYNCHRONIZED WRAPPERS (LESS EFFICIENT) ⚠️
 * 
 * Problem:
 * - Collections.synchronizedMap() wraps HashMap with synchronization
 * - Coarse-grained locking (entire map is locked)
 * - Lower concurrency (only one thread can access map at a time)
 * - Compound operations still need external synchronization
 * 
 * Issues:
 * - All operations lock the entire map
 * - No concurrent reads (even when safe)
 * - Poor performance under high concurrency
 * - Still need synchronized blocks for compound operations
 */
public class InventoryServiceSynchronized {
    
    // ⚠️  Synchronized wrapper - locks entire map for every operation
    private final Map<String, Integer> inventory = Collections.synchronizedMap(new HashMap<>());
    
    public InventoryServiceSynchronized() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }
    
    /**
     * Check if product has enough stock.
     * ⚠️  Synchronized wrapper provides thread-safety, but locks entire map
     */
    public boolean hasStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        if (currentStock == null) {
            return false;
        }
        return currentStock >= quantity;
    }
    
    /**
     * Reserve inventory (decrease stock).
     * ⚠️  Compound operation still needs external synchronization
     * 
     * Problem: Even though map is synchronized, read-modify-write is not atomic
     * Thread 1: Read stock = 10
     * Thread 2: Read stock = 10 (before Thread 1 writes)
     * Thread 1: Write stock = 9
     * Thread 2: Write stock = 9 ❌ Lost update!
     */
    public boolean reserveStock(String productId, int quantity) {
        // ⚠️  Must synchronize on the map for compound operations
        synchronized (inventory) {
            Integer currentStock = inventory.get(productId);
            
            if (currentStock == null || currentStock < quantity) {
                return false;
            }
            
            try {
                Thread.sleep(1); // Simulate processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            
            int newStock = currentStock - quantity;
            inventory.put(productId, newStock);
            
            return true;
        }
    }
    
    /**
     * Get current stock.
     * ⚠️  Synchronized wrapper locks map even for reads
     */
    public int getStock(String productId) {
        Integer stock = inventory.get(productId);
        return stock == null ? 0 : stock;
    }
    
    /**
     * Get all inventory.
     * ⚠️  Must synchronize for iteration
     */
    public Map<String, Integer> getAllInventory() {
        synchronized (inventory) {
            return new HashMap<>(inventory);
        }
    }
}

