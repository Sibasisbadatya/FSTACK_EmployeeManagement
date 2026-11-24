package com.example.emplyee_management.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentDTO(
        @NotNull(message = "Quantity change is required")
        Integer quantityChange,

        String reason
) {}
