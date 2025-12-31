# Multi-Threaded Log Processing System

## 🎯 Project Overview

A **production-ready, multi-threaded log processing system** built step-by-step to deeply understand Java concurrency, threads, worker threads, and multithreading concepts.

This system simulates a real-world backend service that:
- Receives logs from multiple producers (apps/servers)
- Queues incoming logs using thread-safe queues
- Processes logs concurrently using worker threads
- Updates shared metrics (counts, alerts) safely
- Supports graceful shutdown
- Handles high load efficiently

---

## 📚 Learning Path - 8 Steps

### STEP 1: Thread Fundamentals
**Concepts:** What a thread is, Runnable vs Thread, what `start()` does, why worker threads exist

**Files:**
- `Log.java` - Simple log model
- `LogWorker.java` - Single worker thread demonstration
- `LogProcessingSystem.java` - Main class

**Key Learnings:**
- `Thread.start()` creates a new OS thread
- `run()` executes in the new thread, not the current one
- Threads run concurrently and share memory

---

### STEP 2: Producer-Consumer Model
**Concepts:** Why logs must be queued, `wait()`/`notifyAll()`, how threads block & wake up

**Files:**
- `LogQueue.java` - Thread-safe queue with wait/notify
- `LogProducerWorker.java` - Producer threads
- `LogConsumerWorker.java` - Consumer threads (updated)

**Key Learnings:**
- `wait()` releases lock and blocks thread efficiently
- `notifyAll()` wakes waiting threads
- Producer-Consumer pattern decouples work generation from processing

---

### STEP 3: Worker Thread Pool
**Concepts:** Why not create unlimited threads, ExecutorService, thread reuse, task queue behavior

**Files:**
- `LogProcessingService.java` - Uses ExecutorService with thread pool

**Key Learnings:**
- ExecutorService manages reusable thread pool
- Threads are reused for multiple tasks (not destroyed)
- Tasks queue when all threads are busy

---

### STEP 4: Shared State & Safety
**Concepts:** `synchronized` blocks, thread-safe collections, why shared metrics need protection

**Files:**
- `ProcessingMetrics.java` - Thread-safe metrics collector
- `Log.java` - Enhanced with log levels

**Key Learnings:**
- `AtomicInteger` for thread-safe counters (lock-free)
- `ConcurrentHashMap` for thread-safe maps
- `synchronized` blocks for complex operations

---

### STEP 5: Visibility & Shutdown
**Concepts:** `volatile` shutdown flag, how threads see updates, graceful termination

**Files:**
- `LogProcessingService.java` - Enhanced with volatile and graceful shutdown

**Key Learnings:**
- `volatile` ensures visibility across threads
- Graceful shutdown: stop → finish → cleanup
- `CountDownLatch` coordinates shutdown

---

### STEP 6: Callable & Future
**Concepts:** When a task must return a result, alert evaluation from logs

**Files:**
- `AlertEvaluationService.java` - Alert evaluation using Callable/Future

**Key Learnings:**
- `Callable<T>` returns a value (unlike Runnable)
- `Future<T>` represents async computation result
- `get()` blocks until result available (use with timeout)

---

### STEP 7: Interrupts & Cancellation
**Concepts:** How threads are stopped safely, why interrupts are cooperative

**Files:**
- `LogProcessingService.java` - Proper interrupt handling
- `AlertEvaluationService.java` - Cancellation support

**Key Learnings:**
- Interrupts are cooperative - threads must check and respond
- Always restore interrupt status: `Thread.currentThread().interrupt()`
- `Future.cancel(true)` sends interrupt to running thread

---

### STEP 8: Performance & Starvation
**Concepts:** CPU vs I/O tasks, thread pool sizing, preventing worker starvation

**Files:**
- `LogProcessingService.java` - Performance monitoring and optimization
- `LogProcessingSystem.java` - Complete system with all features

**Key Learnings:**
- I/O-bound: pool size = CPU cores × 2
- CPU-bound: pool size = CPU cores
- Monitor utilization to detect under/over-provisioning
- Fair scheduling prevents starvation

---

## 🏗️ Architecture

```
┌─────────────────┐
│  Producers      │  (Multiple log sources)
│  (APP-1, APP-2) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   LogQueue      │  (Thread-safe queue)
│  (wait/notify)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Worker Thread   │  (Thread pool)
│     Pool        │  (ExecutorService)
└────────┬────────┘
         │
         ├──► ProcessingMetrics (Thread-safe counters)
         │
         └──► AlertEvaluationService (Callable/Future)
```

---

## 🚀 Running the System

### Prerequisites
- Java 8 or higher
- Any IDE (IntelliJ IDEA, Eclipse, VS Code)

### Execution
```bash
# Compile
javac -d . com/logprocessing/*.java

# Run
java com.logprocessing.LogProcessingSystem
```

### Expected Output
- Performance monitor reports every 5 seconds
- Thread utilization metrics
- Starvation warnings (if any)
- Final performance report with recommendations

---

## 📊 Key Features

### ✅ Thread Safety
- Atomic operations for counters
- Thread-safe collections (ConcurrentHashMap)
- Synchronized blocks for complex operations
- Volatile for visibility guarantees

### ✅ Performance Optimization
- Optimal thread pool sizing (I/O-bound: CPU cores × 2)
- Performance monitoring
- Starvation detection
- Fair task distribution

### ✅ Graceful Shutdown
- Volatile shutdown flag
- Proper interrupt handling
- Resource cleanup
- CountDownLatch coordination

### ✅ Production-Ready Patterns
- Producer-Consumer pattern
- Thread pool pattern
- Callable/Future pattern
- Cooperative cancellation

---

## 📈 Performance Metrics

The system tracks:
- **Thread Utilization**: Active threads / Pool size
- **Queue Size**: Tasks waiting for threads
- **Wait/Process Ratio**: Determines if task is CPU or I/O bound
- **Peak Metrics**: Maximum active threads, queue size
- **Processing Times**: Average, min, max

---

## 🎓 Concurrency Concepts Covered

1. **Thread Fundamentals**
   - Thread creation and execution
   - Thread lifecycle
   - Concurrent execution

2. **Synchronization**
   - `synchronized` blocks
   - `wait()` and `notifyAll()`
   - Thread coordination

3. **Thread Pools**
   - ExecutorService
   - Thread reuse
   - Task queuing

4. **Thread Safety**
   - Atomic operations
   - Thread-safe collections
   - Shared state protection

5. **Visibility**
   - `volatile` keyword
   - Memory model guarantees
   - Thread communication

6. **Asynchronous Tasks**
   - Callable and Future
   - Result retrieval
   - Exception handling

7. **Cancellation**
   - Interrupts
   - Cooperative cancellation
   - Clean shutdown

8. **Performance**
   - Pool sizing strategies
   - Starvation prevention
   - Performance tuning

---

## 🔧 Configuration

### Thread Pool Size
```java
int poolSize = Runtime.getRuntime().availableProcessors() * 2;
```
- Adjust based on your workload (CPU vs I/O bound)
- Monitor utilization and tune accordingly

### Queue Size
```java
LogQueue logQueue = new LogQueue(50);
```
- Larger queue = more buffering during bursts
- Smaller queue = faster backpressure

### Batch Size
```java
private static final int BATCH_SIZE = 10;
```
- Number of logs before alert evaluation
- Balance between latency and throughput

---

## 📝 Code Quality

- ✅ Clean, production-quality Java code
- ✅ Comprehensive documentation
- ✅ Proper error handling
- ✅ Thread-safe design
- ✅ No intentional bugs
- ✅ Graceful shutdown

---

## 🎯 Real-World Applications

This system demonstrates patterns used in:
- **Log Aggregation Services** (ELK, Splunk)
- **Message Queue Processors** (Kafka consumers)
- **API Request Handlers** (Web servers)
- **Data Pipeline Systems** (ETL processes)
- **Monitoring Systems** (Metrics collectors)

---

## 📚 Further Reading

- Java Concurrency in Practice (Brian Goetz)
- Java Memory Model (JSR 133)
- ExecutorService Javadoc
- Thread Pool Best Practices

---

## 🎉 Project Status

**COMPLETE** - All 8 steps successfully implemented!

This is a production-ready, educational multi-threaded log processing system that demonstrates real-world concurrency patterns and best practices.

---

## 👨‍💻 Author

Built as a comprehensive learning project to understand Java concurrency deeply.

---

## 📄 License

Educational project - feel free to use and modify for learning purposes.

