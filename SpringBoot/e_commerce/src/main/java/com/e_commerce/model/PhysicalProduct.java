package com.e_commerce.model;

public class PhysicalProduct extends Product implements Taxable {
    private Double weight;
    private String shippingAddress;
    
    // Constructors
    public PhysicalProduct() {
        super();
    }
    
    public PhysicalProduct(Long id, String name, Double price, Double weight, String shippingAddress) {
        super(id, name, price);
        this.weight = weight;
        this.shippingAddress = shippingAddress;
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
    
    // Implement Taxable interface methods
    @Override
    public double calculateTax() {
        return getPrice() * getTaxRate();
    }
    
    @Override
    public double getTaxRate() {
        return 0.10; // 10% tax for physical products
    }
}