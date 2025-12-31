package com.logprocessing;

/**
 * STEP 4: Updated Producer with Log Levels
 * 
 * Generates logs with different levels (ERROR, WARNING, INFO) for metrics tracking.
 */
public class LogProducerWorker extends Thread {
    
    private final LogQueue logQueue;
    private final String producerId;
    private volatile boolean running = true;
    private int producedCount = 0;
    
    public LogProducerWorker(LogQueue logQueue, String producerId) {
        this.logQueue = logQueue;
        this.producerId = producerId;
        this.setName("Producer-" + producerId);
    }
    
    @Override
    public void run() {
        System.out.println(
            String.format(
                "[Producer Thread %s] Started. Thread ID: %d",
                producerId,
                Thread.currentThread().getId()
            )
        );
        
        while (running) {
            try {
                // Generate log with random level
                String level = Log.randomLevel();
                Log log = new Log(
                    producerId + "-LOG-" + (producedCount + 1),
                    "Log message from " + producerId,
                    level,
                    producerId
                );
                
                logQueue.addLog(log);
                producedCount++;
                
                Thread.sleep(400); // Logs arrive every 400ms
                
            } catch (InterruptedException e) {
                System.out.println(
                    String.format("[Producer Thread %s] Interrupted, stopping...", producerId)
                );
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println(
            String.format(
                "[Producer Thread %s] Stopped. Total produced: %d",
                producerId,
                producedCount
            )
        );
    }
    
    public void stopProducer() {
        this.running = false;
        this.interrupt();
    }
    
    public int getProducedCount() {
        return producedCount;
    }
}