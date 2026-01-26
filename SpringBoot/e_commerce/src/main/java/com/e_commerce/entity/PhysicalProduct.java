package com.e_commerce.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PhysicalProduct extends Product {
    private Double weight;
    private String shippingAddress;
    
    @Override
    public String getProductType() {
        return "Physical";
    }
}

