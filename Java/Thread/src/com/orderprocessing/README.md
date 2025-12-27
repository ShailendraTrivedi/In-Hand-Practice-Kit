# Multi-Threaded Order Processing Backend

A production-ready Java backend system demonstrating all major Java multithreading and concurrency concepts through a realistic e-commerce order processing service.

## Overview

This system simulates a real backend service that handles concurrent order submissions, manages inventory, processes payments asynchronously, and performs graceful shutdown under heavy load.

## Architecture

```
OrderProcessingSystem (Main Entry)
    ├── OrderManager (Orchestrator)
    │   ├── OrderQueue (Producer-Consumer)
    │   ├── Worker Thread Pool (CPU-bound)
    │   ├── Payment Thread Pool (I/O-bound)
    │   ├── InventoryService (Shared Resource)
    │   └── PaymentService (Async Processing)
    └── ShutdownManager (Graceful Termination)
```

## Key Components

### Core Classes

- **OrderProcessingSystem.java** - Main entry point, simulates high-load order submission
- **OrderManager.java** - Manages thread pools and system lifecycle
- **OrderQueue.java** - Thread-safe queue with Producer-Consumer pattern
- **OrderWorker.java** - Worker threads that process orders
- **InventoryService.java** - Thread-safe inventory management
- **PaymentService.java** - Asynchronous payment processing
- **ShutdownManager.java** - Coordinates graceful shutdown

## Concurrency Concepts Demonstrated

1. **Thread & Runnable** - Worker threads for concurrent order processing
2. **synchronized blocks/methods** - Inventory operations protection
3. **volatile** - Shutdown flags and order status visibility
4. **wait()/notifyAll()** - Producer-Consumer queue coordination
5. **ExecutorService** - Thread pool management
6. **Callable & Future** - Async payment processing with timeouts
7. **Interrupts** - Graceful task cancellation
8. **Thread-safe collections** - ConcurrentHashMap for inventory
9. **Deadlock prevention** - Consistent lock ordering and timeouts
10. **Thread starvation prevention** - Fair thread pool sizing
11. **Performance tuning** - Separate pools for CPU vs I/O tasks
12. **Proper shutdown handling** - Multi-phase graceful termination

## How to Run

### Compile

```bash
javac -d out -sourcepath src src/com/orderprocessing/**/*.java
```

### Execute

```bash
java -cp out com.orderprocessing.OrderProcessingSystem
```

### Expected Behavior

- Submits 5000 orders from 10 concurrent producer threads
- Processes orders through worker threads
- Handles payments asynchronously
- Displays periodic statistics
- Performs graceful shutdown
- Shows final inventory status

## Configuration

Modify constants in `OrderProcessingSystem.java`:

```java
private static final int NUM_ORDERS = 5000;           // Total orders
private static final int NUM_WORKERS = ...;          // CPU cores
private static final int PAYMENT_THREADS = ...;      // I/O threads
private static final int QUEUE_SIZE = 1000;          // Queue capacity
```

## Features

✅ Thread-safe operations  
✅ Graceful shutdown  
✅ Deadlock prevention  
✅ Resource leak prevention  
✅ Proper exception handling  
✅ Performance optimization  
✅ Scalable architecture  

## Project Structure

```
src/com/orderprocessing/
├── model/
│   └── Order.java
├── service/
│   ├── InventoryService.java
│   └── PaymentService.java
├── queue/
│   └── OrderQueue.java
├── worker/
│   ├── OrderWorker.java
│   └── OrderProcessor.java
├── manager/
│   ├── OrderManager.java
│   └── ShutdownManager.java
├── OrderProcessingSystem.java
└── README.md
```

