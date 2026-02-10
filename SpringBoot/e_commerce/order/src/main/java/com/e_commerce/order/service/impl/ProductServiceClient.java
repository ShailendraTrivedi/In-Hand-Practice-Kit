package com.e_commerce.order.service.impl;

import com.e_commerce.order.dto.ProductDto;
import com.e_commerce.order.exception.ProductNotFoundException;
import com.e_commerce.order.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceClient implements IProductService {

    private final RestTemplate restTemplate;

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

            throw new ProductNotFoundException(id);

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Product not found with id: {}", id);
            throw new ProductNotFoundException(id);
        } catch (HttpClientErrorException e) {
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString(), id);
            log.error("HTTP error calling product service for product id: {}, status: {}", id, e.getStatusCode());
            throw new ProductNotFoundException(errorMessage);
        } catch (HttpServerErrorException e) {
            // Handle 500 errors from product service (e.g., ProductNotFoundException returning 500)
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString(), id);
            if (errorMessage.contains("not found") || errorMessage.contains("Product with id")) {
                log.error("Product not found with id: {}", id);
                throw new ProductNotFoundException(id);
            }
            log.error("Server error calling product service for product id: {}, status: {}", id, e.getStatusCode());
            throw new RuntimeException("Product service error: " + errorMessage);
        } catch (RestClientException e) {
            log.error("Error communicating with product service for product id: {}", id, e);
            throw new RuntimeException("Unable to connect to product service. Please try again later.");
        }
    }

    private String extractErrorMessage(String responseBody, Long productId) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "Product with id " + productId + " not found";
        }

        // Try to extract message from JSON response
        // Look for "message" field in JSON: "message":"Product with id X not found"
        String messagePattern = "\"message\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(messagePattern);
        java.util.regex.Matcher matcher = pattern.matcher(responseBody);
        
        if (matcher.find()) {
            String extractedMessage = matcher.group(1);
            // Remove "An unexpected error occurred: " prefix if present
            if (extractedMessage.startsWith("An unexpected error occurred: ")) {
                extractedMessage = extractedMessage.substring("An unexpected error occurred: ".length());
            }
            // If message contains product not found info, use it
            if (extractedMessage.contains("Product with id") || extractedMessage.contains("not found")) {
                return extractedMessage;
            }
        }

        // Fallback: check if response contains product not found message
        if (responseBody.contains("\"Product with id") || responseBody.contains("not found")) {
            return "Product with id " + productId + " not found";
        }

        return "Product with id " + productId + " not found";
    }
}