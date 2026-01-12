package com.inventoryProcessing.Concept9;

import java.util.HashMap;
import java.util.Map;

public class PaymentService {
    private final Map<String, Double> userBalances = new HashMap<>();
    private final Object lock = new Object();

    public PaymentService() {
        // Initialize some user balances
        userBalances.put("USER-101", 1000.0);
        userBalances.put("USER-102", 500.0);
        userBalances.put("USER-103", 2000.0);
        userBalances.put("USER-104", 750.0);
        userBalances.put("USER-105", 1500.0);
    }

    public Object getLock() {
        return lock;
    }

    public boolean processPayment(String userId, double amount) {
        synchronized (lock) {
            Double balance = userBalances.get(userId);

            if (balance == null || balance < amount) {
                return false;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            double newBalance = balance - amount;
            userBalances.put(userId, newBalance);

            return true;
        }
    }

    public synchronized double getBalance(String userId) {
        Double balance = userBalances.get(userId);
        return balance == null ? 0.0 : balance;
    }

    public boolean hasBalance(String userId, double amount) {
        synchronized(lock) {
            Double balance = userBalances.get(userId);
            return balance != null && balance >= amount;
        }
    }
}
