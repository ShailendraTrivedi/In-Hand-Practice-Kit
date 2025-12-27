package com.taskmanager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Task scheduler demonstrating deadlock and prevention.
 * Concept 6: Deadlock (intentional + prevention)
 */
public class TaskScheduler {
    private final Object lockA = new Object();
    private final Object lockB = new Object();
    
    // For deadlock prevention
    private final Lock lock1 = new ReentrantLock();
    private final Lock lock2 = new ReentrantLock();
    
    /**
     * DEADLOCK VERSION - Intentional bug
     * 
     * DEADLOCK SCENARIO:
     * Thread 1: acquires lockA, then tries lockB
     * Thread 2: acquires lockB, then tries lockA
     * Result: Both threads wait forever
     */
    public void scheduleTaskDeadlock(Task task, boolean order) {
        if (order) {
            synchronized (lockA) {
                System.out.println("Thread " + Thread.currentThread().getName() + " acquired lockA");
                try {
                    Thread.sleep(100); // Increase chance of deadlock
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                synchronized (lockB) {
                    System.out.println("Thread " + Thread.currentThread().getName() + " acquired lockB");
                    // Process task
                    processTask(task);
                }
            }
        } else {
            synchronized (lockB) {
                System.out.println("Thread " + Thread.currentThread().getName() + " acquired lockB");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                synchronized (lockA) {
                    System.out.println("Thread " + Thread.currentThread().getName() + " acquired lockA");
                    processTask(task);
                }
            }
        }
    }
    
    /**
     * DEADLOCK PREVENTION - Always acquire locks in same order
     * 
     * FIX: Always acquire locks in the same order (lock1 then lock2)
     * This prevents circular wait condition
     */
    public void scheduleTaskFixed(Task task) {
        // Always acquire locks in same order
        lock1.lock();
        try {
            System.out.println("Thread " + Thread.currentThread().getName() + " acquired lock1");
            Thread.sleep(50);
            
            lock2.lock();
            try {
                System.out.println("Thread " + Thread.currentThread().getName() + " acquired lock2");
                processTask(task);
            } finally {
                lock2.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock1.unlock();
        }
    }
    
    /**
     * ALTERNATIVE: Try-lock with timeout (prevents indefinite blocking)
     */
    public void scheduleTaskWithTimeout(Task task) {
        boolean acquired1 = false;
        boolean acquired2 = false;
        
        try {
            acquired1 = lock1.tryLock(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!acquired1) {
                System.out.println("Could not acquire lock1, aborting");
                return;
            }
            
            acquired2 = lock2.tryLock(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!acquired2) {
                System.out.println("Could not acquire lock2, aborting");
                return;
            }
            
            processTask(task);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (acquired2) lock2.unlock();
            if (acquired1) lock1.unlock();
        }
    }
    
    private void processTask(Task task) {
        System.out.println("Processing task " + task.getId() + " in scheduler");
        task.setStatus(TaskStatus.PROCESSING);
    }
}