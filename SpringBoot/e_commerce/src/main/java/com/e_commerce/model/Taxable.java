package com.e_commerce.model;

public interface Taxable {
    double calculateTax();
    double getTaxRate();
}