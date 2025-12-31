package com.logprocessing;

import java.util.Random;

/**
 * STEP 4: Enhanced Log Model with Levels
 * 
 * Adds log level and source information for metrics tracking.
 */
public class Log {
    private final String id;
    private final String message;
    private final long timestamp;
    private final String level; // ERROR, WARNING, INFO
    private final String source; // Source application/service
    
    public Log(String id, String message) {
        this.id = id;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        // Default values
        this.level = "INFO";
        this.source = "UNKNOWN";
    }
    
    public Log(String id, String message, String level, String source) {
        this.id = id;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.level = level;
        this.source = source;
    }
    
    public String getId() { return id; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getSource() { return source; }
    
    @Override
    public String toString() {
        return String.format("Log[id=%s, level=%s, source=%s, msg=%s]", 
            id, level, source, message);
    }
    
    /**
     * Helper method to generate random log levels for demonstration.
     */
    public static String randomLevel() {
        Random random = new Random();
        int rand = random.nextInt(100);
        if (rand < 5) return "ERROR";      // 5% errors
        if (rand < 20) return "WARNING";    // 15% warnings
        return "INFO";                      // 80% info
    }
}