package com.taskmanager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task manager with intentional race condition bug and fix.
 * Demonstrates: Race conditions, synchronized, volatile, thread-safe collections
 */
public class TaskManager {
    // Concept 12: Thread-safe collections
    private final ConcurrentHashMap<Integer, Task> tasks;
    
    // BUGGY VERSION: Race condition on counter
    private int completedCount = 0; // NOT thread-safe!
    
    // FIXED VERSION: Atomic counter
    private final AtomicInteger completedCountFixed = new AtomicInteger(0);
    
    // Concept 4: volatile for visibility
    private volatile boolean systemActive = true;
    
    public TaskManager() {
        this.tasks = new ConcurrentHashMap<>();
    }
    
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }
    
    public Task getTask(int id) {
        return tasks.get(id);
    }
    
    /**
     * BUGGY METHOD - Race condition!
     * Concept 2: Race condition demonstration
     * 
     * PROBLEM: Multiple threads can read, increment, and write simultaneously.
     * This leads to lost updates.
     */
    public void incrementCompletedBuggy() {
        // BUG: Not atomic! Multiple threads can execute this simultaneously
        completedCount++; // Read -> Increment -> Write (not atomic)
    }
    
    /**
     * FIXED METHOD - Using synchronized
     * Concept 3: synchronized method
     */
    public synchronized void incrementCompletedFixed() {
        completedCountFixed.incrementAndGet();
    }
    
    /**
     * ALTERNATIVE FIX - Using AtomicInteger (better performance)
     */
    public void incrementCompletedAtomic() {
        completedCountFixed.incrementAndGet(); // Atomic operation
    }
    
    public int getCompletedCountBuggy() {
        return completedCount; // May show incorrect value due to race condition
    }
    
    public int getCompletedCountFixed() {
        return completedCountFixed.get();
    }
    
    /**
     * Concept 4: volatile ensures visibility across threads
     */
    public boolean isSystemActive() {
        return systemActive; // All threads see latest value
    }
    
    public void setSystemActive(boolean active) {
        this.systemActive = active; // Change visible to all threads immediately
    }
    
    public int getTotalTasks() {
        return tasks.size();
    }
}