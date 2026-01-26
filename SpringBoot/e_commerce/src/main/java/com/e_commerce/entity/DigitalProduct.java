package com.e_commerce.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DigitalProduct extends Product {
    private String downloadLink;
    private Long fileSizeInMB;
    
    @Override
    public String getProductType() {
        return "Digital";
    }
}

