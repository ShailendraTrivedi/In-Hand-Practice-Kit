package com.e_commerce.order.service.impl;

import com.e_commerce.order.dto.ProductDto;
import com.e_commerce.order.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceClient implements IProductService {

    private final RestTemplate restTemplate;  // Note: it's "restTemplate", not "client"

    @Value("${product.service.url:http://localhost:8081}")
    private String productServiceUrl;

    @Override
    public ProductDto getProductById(Long id) {
        try {
            String url = productServiceUrl + "/products/" + id;
            log.info("Calling product service: {}", url);

            ResponseEntity<ProductDto> response = restTemplate.getForEntity(url, ProductDto.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }

            throw new RuntimeException("Product not found with id: " + id);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("Product not found with id: {}", id);
                throw new RuntimeException("Product not found with id: " + id, e);
            }
            log.error("HTTP error calling product service for product id: {}, status: {}", id, e.getStatusCode(), e);
            throw new RuntimeException("Error calling product service: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Error calling product service for product id: {}", id, e);
            throw new RuntimeException("Error communicating with product service: " + e.getMessage(), e);
        }
    }
}