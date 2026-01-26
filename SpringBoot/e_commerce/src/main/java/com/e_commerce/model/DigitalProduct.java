package com.e_commerce.model;

public class DigitalProduct extends Product {
    private String downloadLink;
    private Long fileSizeInMB;
    
    public DigitalProduct() {
        super();
    }
    
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
    
    @Override
    public String getProductType() {
        return "Digital";
    }
}