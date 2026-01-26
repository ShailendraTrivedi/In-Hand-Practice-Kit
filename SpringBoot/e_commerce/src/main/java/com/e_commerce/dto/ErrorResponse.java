package com.e_commerce.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    
    public ErrorResponse(int status, String errorCode, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }
    
    // Getters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getPath() {
        return path;
    }
}