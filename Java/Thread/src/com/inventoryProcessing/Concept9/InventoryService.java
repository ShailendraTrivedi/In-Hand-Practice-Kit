package com.inventoryProcessing.Concept9;

import java.util.HashMap;
import java.util.Map;

/**
 * InventoryService for Concept #9.
 * Self-contained within Concept9 folder.
 * Uses synchronized methods for demonstration.
 */
public class InventoryService {
    
    private final Map<String, Integer> inventory = new HashMap<>();
    private final Object lock = new Object(); // Lock object for deadlock demo
    
    public InventoryService() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }
    
    /**
     * Get the lock object (for deadlock demonstration).
     */
    public Object getLock() {
        return lock;
    }
    
    /**
     * Reserve inventory (decrease stock).
     * Uses synchronized block for deadlock demonstration.
     */
    public boolean reserveStock(String productId, int quantity) {
        synchronized (lock) {
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
     * Get current stock.
     */
    public synchronized int getStock(String productId) {
        Integer stock = inventory.get(productId);
        return stock == null ? 0 : stock;
    }
    
    /**
     * Check if product has enough stock.
     */
    public boolean hasStock(String productId, int quantity) {
        synchronized (lock) {
            Integer currentStock = inventory.get(productId);
            return currentStock != null && currentStock >= quantity;
        }
    }
}