package com.inventoryProcessing.Concept2;

import java.util.HashMap;
import java.util.Map;

/**
 * CONCEPT #2 - BEFORE: InventoryService WITHOUT synchronization
 * 
 * ⚠️  THIS VERSION HAS RACE CONDITIONS ⚠️
 * 
 * This demonstrates the PROBLEM:
 * - Multiple threads can read and write inventory simultaneously
 * - This leads to lost updates and incorrect stock counts
 * - Can result in overselling (negative inventory)
 * 
 * Race Condition Scenario:
 * Thread 1: Reads stock = 10
 * Thread 2: Reads stock = 10 (before Thread 1 updates)
 * Thread 1: Writes stock = 9 (10 - 1)
 * Thread 2: Writes stock = 9 (10 - 1) ❌ Should be 8!
 * 
 * Result: Two orders processed, but stock only decreased by 1 instead of 2
 */
public class InventoryServiceUnsafe {
    
    // Shared state: Product ID -> Stock quantity
    // ⚠️  NOT THREAD-SAFE - Multiple threads can modify this simultaneously
    private final Map<String, Integer> inventory = new HashMap<>();
    
    /**
     * Initialize inventory with some products.
     */
    public InventoryServiceUnsafe() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }
    
    /**
     * Check if product has enough stock.
     * ⚠️  RACE CONDITION: Another thread can modify inventory between check and reserve
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
     * ⚠️  RACE CONDITION: Not atomic - read, modify, write can be interleaved
     * 
     * Example of race condition:
     * Thread 1: Read stock = 10
     * Thread 2: Read stock = 10 (before Thread 1 writes)
     * Thread 1: Calculate newStock = 9, Write 9
     * Thread 2: Calculate newStock = 9, Write 9 ❌ Lost update!
     */
    public boolean reserveStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        
        if (currentStock == null || currentStock < quantity) {
            return false; // Not enough stock
        }
        
        // ⚠️  THIS IS NOT ATOMIC - Multiple threads can execute this simultaneously
        // Simulate some processing time (makes race condition more likely)
        try {
            Thread.sleep(1); // Simulate database/network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int newStock = currentStock - quantity;
        inventory.put(productId, newStock);
        
        return true;
    }
    
    /**
     * Get current stock (for display purposes).
     * ⚠️  Can read stale data if another thread is updating
     */
    public int getStock(String productId) {
        Integer stock = inventory.get(productId);
        return stock == null ? 0 : stock;
    }
    
    /**
     * Get all inventory (for display).
     */
    public Map<String, Integer> getAllInventory() {
        return new HashMap<>(inventory); // Return copy to prevent external modification
    }
}