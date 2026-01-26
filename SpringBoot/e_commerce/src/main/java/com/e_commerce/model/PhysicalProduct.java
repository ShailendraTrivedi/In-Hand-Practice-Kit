package com.e_commerce.model;

public class PhysicalProduct extends Product {
    private Double weight;
    private String shippingAddress;
    
    public PhysicalProduct() {
        super();
    }
    
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
    
    @Override
    public String getProductType() {
        return "Physical";
    }
}