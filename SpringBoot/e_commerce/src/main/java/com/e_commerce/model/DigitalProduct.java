package com.e_commerce.model;

public class DigitalProduct extends Product {
    private String downloadLink;
    private Long fileSizeInMB;
    
    // Constructors
    public DigitalProduct() {
        super();
    }
    
    public DigitalProduct(Long id, String name, Double price, String downloadLink, Long fileSizeInMB) {
        super(id, name, price);
        this.downloadLink = downloadLink;
        this.fileSizeInMB = fileSizeInMB;
    }
    
    // Getters and Setters
    public String getDownloadLink() {
        return downloadLink;
    }
    
    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }
    
    public Long getFileSizeInMB() {
        return fileSizeInMB;
    }
    
    public void setFileSizeInMB(Long fileSizeInMB) {
        this.fileSizeInMB = fileSizeInMB;
    }
    
    // Implement abstract method from Product
    @Override
    public String getProductType() {
        return "Digital";
    }
}