package com.taskmanager;

/**
 * Worker thread that processes tasks.
 * Demonstrates: Thread, Runnable, Interrupts
 */
public class Worker implements Runnable {
    private final int workerId;
    private final TaskQueue taskQueue;
    private volatile boolean running = true; // Concept 4: volatile for visibility
    
    public Worker(int workerId, TaskQueue taskQueue) {
        this.workerId = workerId;
        this.taskQueue = taskQueue;
    }
    
    /**
     * Concept 1: Runnable interface implementation
     * Concept 9: Interrupts for graceful cancellation
     */
    @Override
    public void run() {
        System.out.println("Worker " + workerId + " started");
        
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Concept 5: Blocking call that respects interrupts
                Task task = taskQueue.dequeue();
                
                // Check if interrupted before processing
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Worker " + workerId + " interrupted, stopping");
                    break;
                }
                
                processTask(task);
                
            } catch (InterruptedException e) {
                // Concept 9: Handle interrupt gracefully
                System.out.println("Worker " + workerId + " interrupted during wait");
                Thread.currentThread().interrupt(); // Restore interrupt status
                break;
            }
        }
        
        System.out.println("Worker " + workerId + " stopped");
    }
    
    private void processTask(Task task) {
        if (task.isCancelled()) {
            System.out.println("Worker " + workerId + " skipping cancelled task " + task.getId());
            return;
        }
        
        task.setStatus(TaskStatus.PROCESSING);
        System.out.println("Worker " + workerId + " processing task " + task.getId());
        
        try {
            // Simulate work
            Thread.sleep((long) (Math.random() * 1000));
            
            if (Math.random() < 0.1) { // 10% failure rate
                throw new RuntimeException("Task processing failed");
            }
            
            task.setStatus(TaskStatus.COMPLETED);
            System.out.println("Worker " + workerId + " completed task " + task.getId());
            
        } catch (InterruptedException e) {
            // Concept 9: Handle interrupt during processing
            System.out.println("Worker " + workerId + " interrupted while processing task " + task.getId());
            task.setStatus(TaskStatus.CANCELLED);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            System.out.println("Worker " + workerId + " failed task " + task.getId() + ": " + e.getMessage());
        }
    }
    
    public void stop() {
        this.running = false;
    }
}
