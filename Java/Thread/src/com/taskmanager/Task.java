package com.taskmanager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a task in the system.
 * Uses AtomicInteger to demonstrate thread-safe counters.
 */
public class Task {
    private static final AtomicInteger taskIdGenerator = new AtomicInteger(0);
    
    private final int id;
    private final String description;
    private volatile TaskStatus status; // Concept 4: volatile for visibility
    private volatile boolean cancelled; // volatile ensures visibility across threads
    
    public Task(String description) {
        this.id = taskIdGenerator.incrementAndGet();
        this.description = description;
        this.status = TaskStatus.PENDING;
        this.cancelled = false;
    }
    
    public int getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void cancel() {
        this.cancelled = true;
        this.status = TaskStatus.CANCELLED;
    }
}