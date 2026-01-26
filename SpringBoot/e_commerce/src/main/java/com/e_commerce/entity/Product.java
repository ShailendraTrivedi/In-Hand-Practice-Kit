package com.e_commerce.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "name", "price"})
public abstract class Product {
    private Long id;
    private String name;
    private Double price;
    
    public abstract String getProductType();
}

