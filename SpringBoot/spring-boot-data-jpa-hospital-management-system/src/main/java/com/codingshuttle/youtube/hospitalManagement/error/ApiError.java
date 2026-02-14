package com.codingshuttle.youtube.hospitalManagement.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiError {
    private LocalDateTime timeStamp;
    private String error;
    private HttpStatus statusCode;
    private String path;
    private Object details;

    public ApiError() {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiError(String error, HttpStatus statusCode) {
        this();
        this.error = error;
        this.statusCode = statusCode;
    }

    public ApiError(String error, HttpStatus statusCode, String path) {
        this(error, statusCode);
        this.path = path;
    }
}