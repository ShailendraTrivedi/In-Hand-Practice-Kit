# Multi-Threaded Task Management System

A Java demonstration project showcasing core multithreading and concurrency concepts through a task management system with both buggy and fixed implementations.

## Overview

This project demonstrates common concurrency problems (race conditions, deadlocks) and their solutions using proper Java concurrency mechanisms. It includes both intentionally buggy code to show problems and fixed implementations.

## Key Components

### Core Classes

- **ConcurrencyDemo.java** - Main demonstration class with multiple scenarios
- **TaskManager.java** - Demonstrates race conditions and synchronized fixes
- **TaskQueue.java** - Producer-Consumer pattern with wait/notify
- **Worker.java** - Worker threads implementing Runnable
- **TaskProcessor.java** - ExecutorService with Callable/Future
- **TaskScheduler.java** - Deadlock demonstration and prevention
- **Task.java** - Task domain model
- **TaskStatus.java** - Task status enumeration

## Concurrency Concepts Demonstrated

### 1. Thread & Runnable
- **Location**: `Worker.java`
- **Demonstration**: Worker threads processing tasks from queue
- **Key Point**: Runnable interface for defining thread tasks

### 2. Race Conditions
- **Location**: `TaskManager.java`
- **Buggy Version**: `incrementCompletedBuggy()` - loses updates
- **Fixed Version**: `incrementCompletedFixed()` - synchronized method
- **Alternative Fix**: `incrementCompletedAtomic()` - AtomicInteger

### 3. synchronized blocks/methods
- **Location**: `TaskQueue.java`, `TaskManager.java`
- **Usage**: Protecting critical sections from concurrent access
- **Key Point**: Ensures atomic operations

### 4. volatile
- **Location**: `TaskManager.java`, `Worker.java`
- **Usage**: `systemActive` and `running` flags for visibility
- **Key Point**: Ensures all threads see latest value immediately

### 5. wait() / notifyAll() (Producer-Consumer)
- **Location**: `TaskQueue.java`
- **Pattern**: Producer threads enqueue, consumer threads dequeue
- **Key Point**: Efficient thread coordination without busy-waiting

### 6. Deadlock Prevention
- **Location**: `TaskScheduler.java`
- **Buggy Version**: `scheduleTaskDeadlock()` - causes deadlock (commented out)
- **Fixed Version**: `scheduleTaskFixed()` - consistent lock ordering
- **Key Point**: Always acquire locks in same order

### 7. ExecutorService
- **Location**: `TaskProcessor.java`
- **Usage**: Fixed thread pool for task processing
- **Key Point**: Manages thread lifecycle automatically

### 8. Callable & Future
- **Location**: `TaskProcessor.java`
- **Usage**: Submit tasks and retrieve results asynchronously
- **Key Point**: `Future.get()` blocks until result available

### 9. Interrupts
- **Location**: `Worker.java`
- **Usage**: Graceful thread cancellation
- **Key Point**: Check `isInterrupted()` and handle `InterruptedException`

### 10. Thread-safe Collections
- **Location**: `TaskManager.java`
- **Usage**: `ConcurrentHashMap` for thread-safe task storage
- **Key Point**: Lock-free reads, fine-grained locking for writes

### 11. Proper Shutdown Handling
- **Location**: `TaskProcessor.java`
- **Process**: 
  1. `shutdown()` - stop accepting new tasks
  2. `awaitTermination()` - wait for completion
  3. `shutdownNow()` - force shutdown if timeout
- **Key Point**: Ensures all tasks complete before termination

## How to Run

### Compile

```bash
javac -d out -sourcepath src src/com/taskmanager/*.java
```

### Execute

```bash
java -cp out com.taskmanager.ConcurrencyDemo
```

### Expected Output

The demo runs 6 different scenarios:

1. **Race Condition Demo** - Shows buggy vs fixed counter increment
2. **Producer-Consumer Demo** - Multiple workers processing tasks from queue
3. **ExecutorService Demo** - Thread pool processing 50 tasks
4. **Deadlock Demo** - Fixed version (buggy version commented out)
5. **Interrupts Demo** - Graceful thread cancellation
6. **Shutdown Demo** - Proper ExecutorService shutdown

## Learning Points

### Race Condition Example
```java
// BUGGY - Multiple threads can execute simultaneously
public void incrementCompletedBuggy() {
    completedCount++; // Not atomic!
}

// FIXED - Synchronized ensures atomicity
public synchronized void incrementCompletedFixed() {
    completedCountFixed.incrementAndGet();
}
```

### Producer-Consumer Pattern
```java
// Producer waits if queue is full
while (queue.size() >= maxSize) {
    wait(); // Releases lock
}

// Consumer waits if queue is empty
while (queue.isEmpty()) {
    wait(); // Releases lock
}
```

### Deadlock Prevention
- **Problem**: Different lock acquisition order causes deadlock
- **Solution**: Always acquire locks in consistent order
- **Example**: Lock A before Lock B, always

### Graceful Shutdown
```java
executorService.shutdown();           // Stop accepting new tasks
executorService.awaitTermination(...); // Wait for completion
executorService.shutdownNow();        // Force shutdown if needed
```

## Project Structure

```
src/com/taskmanager/
├── ConcurrencyDemo.java    # Main demo class
├── TaskManager.java        # Race condition demo
├── TaskQueue.java          # Producer-Consumer
├── Worker.java             # Worker threads
├── TaskProcessor.java      # ExecutorService demo
├── TaskScheduler.java       # Deadlock prevention
├── Task.java               # Task model
├── TaskStatus.java         # Status enum
└── README.md
```

## Key Differences from Order Processing System

This project focuses on **educational demonstrations** with:
- Intentional bugs to show problems
- Side-by-side buggy vs fixed code
- Multiple scenarios in one demo class
- Clear explanations of each concept

The Order Processing System focuses on **production-ready code** with:
- No intentional bugs
- Real-world architecture
- High-load simulation
- Comprehensive documentation

## Best Practices Demonstrated

✅ Always use synchronized or atomic operations for shared state  
✅ Use volatile for simple flags (visibility)  
✅ Use wait/notify for efficient thread coordination  
✅ Always acquire locks in consistent order  
✅ Handle InterruptedException properly  
✅ Use ExecutorService instead of manual thread management  
✅ Implement graceful shutdown  
✅ Use thread-safe collections when appropriate  

