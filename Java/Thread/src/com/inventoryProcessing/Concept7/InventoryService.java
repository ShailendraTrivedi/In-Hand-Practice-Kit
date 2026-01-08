package com.inventoryProcessing.Concept7;

import java.util.HashMap;
import java.util.Map;

/**
 * InventoryService for Concept #7.
 * Self-contained within Concept7 folder.
 * Thread-safe inventory management.
 */
public class InventoryService {
    
    private final Map<String, Integer> inventory = new HashMap<>();
    
    public InventoryService() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }
    
    public synchronized boolean hasStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        if (currentStock == null) {
            return false;
        }
        return currentStock >= quantity;
    }
    
    public synchronized boolean reserveStock(String productId, int quantity) {
        Integer currentStock = inventory.get(productId);
        
        if (currentStock == null || currentStock < quantity) {
            return false;
        }
        
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
    
    public synchronized int getStock(String productId) {
        Integer stock = inventory.get(productId);
        return stock == null ? 0 : stock;
    }
    
    public synchronized Map<String, Integer> getAllInventory() {
        return new HashMap<>(inventory);
    }
}

