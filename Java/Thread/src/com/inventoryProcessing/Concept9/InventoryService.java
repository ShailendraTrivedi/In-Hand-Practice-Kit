package com.inventoryProcessing.Concept9;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InventoryService for Concept #9.
 * Self-contained within Concept9 folder.
 */
public class InventoryService {
    
    private final ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>();
    private final Object inventoryLock = new Object(); // For deadlock demonstration
    
    public InventoryService() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }
    
    /**
     * Get lock object for deadlock demonstration.
     * ⚠️  Only used in deadlock scenario
     */
    public Object getLock() {
        return inventoryLock;
    }
    
    public boolean hasStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        if (currentStock == null) {
            return false;
        }
        return currentStock >= quantity;
    }
    
    /**
     * Reserve inventory with explicit lock (for deadlock demo).
     * ⚠️  Uses synchronized block for deadlock demonstration
     */
    public boolean reserveStockWithLock(String productId, int quantity) {
        synchronized (inventoryLock) {
            Integer currentStock = inventory.get(productId);
            
            if (currentStock == null || currentStock < quantity) {
                return false;
            }
            
            try {
                Thread.sleep(10); // Simulate processing
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
     * Reserve inventory (thread-safe, no deadlock risk).
     * ✅ Uses ConcurrentHashMap atomic operations
     */
    public boolean reserveStock(String productId, int quantity) {
        return inventory.compute(productId, (key, currentStock) -> {
            if (currentStock == null || currentStock < quantity) {
                return currentStock;
            }
            return currentStock - quantity;
        }) != null && inventory.get(productId) >= 0;
    }
    
    public int getStock(String productId) {
        Integer stock = inventory.get(productId);
        return stock == null ? 0 : stock;
    }
    
    public Map<String, Integer> getAllInventory() {
        return new HashMap<>(inventory);
    }
}

