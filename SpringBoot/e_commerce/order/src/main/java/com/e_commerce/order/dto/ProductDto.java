package com.e_commerce.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {
    private Long id;
    private String name;
    private Double price;
    private String productType;
}