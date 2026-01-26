package com.e_commerce.model;

public class PhysicalProduct extends Product {
    private Double weight;
    private String shippingAddress;
    
    // Constructors
    public PhysicalProduct() {
        super();
    }
    
    // Getters and Setters
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    // Implement abstract method from Product
    @Override
    public String getProductType() {
        return "Physical";
    }
}