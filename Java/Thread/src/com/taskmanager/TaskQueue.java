package com.taskmanager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Thread-safe task queue implementing Producer-Consumer pattern.
 * Demonstrates: wait(), notifyAll(), synchronized blocks
 */
public class TaskQueue {
    private final Queue<Task> queue;
    private final int maxSize;
    
    // Concept 3: synchronized blocks for thread safety
    public TaskQueue(int maxSize) {
        this.queue = new LinkedList<>();
        this.maxSize = maxSize;
    }
    
    /**
     * Producer method - adds tasks to queue
     * Concept 5: wait() / notifyAll() for Producer-Consumer
     */
    public synchronized void enqueue(Task task) throws InterruptedException {
        // Wait if queue is full (backpressure)
        while (queue.size() >= maxSize) {
            System.out.println("Queue full, producer waiting...");
            wait(); // Releases lock and waits
        }
        
        queue.offer(task);
        System.out.println("Task " + task.getId() + " enqueued. Queue size: " + queue.size());
        
        // Notify all waiting consumers
        notifyAll();
    }
    
    /**
     * Consumer method - removes tasks from queue
     * Concept 5: wait() / notifyAll() for Producer-Consumer
     */
    public synchronized Task dequeue() throws InterruptedException {
        // Wait if queue is empty
        while (queue.isEmpty()) {
            wait(); // Releases lock and waits
        }
        
        Task task = queue.poll();
        System.out.println("Task " + task.getId() + " dequeued. Queue size: " + queue.size());
        
        // Notify all waiting producers
        notifyAll();
        
        return task;
    }
    
    public synchronized int size() {
        return queue.size();
    }
    
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}