package com.example.emplyee_management.dto;

import com.example.emplyee_management.entity.Product;

import java.math.BigDecimal;

public record ProductResponseDTO(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        Integer stock,
        Long version
) {
    public static ProductResponseDTO from(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getVersion()
        );
    }
}
