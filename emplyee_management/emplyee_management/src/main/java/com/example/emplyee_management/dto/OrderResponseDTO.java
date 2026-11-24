package com.example.emplyee_management.dto;

import com.example.emplyee_management.entity.Order;
import com.example.emplyee_management.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        Long customerId,
        String customerName,
        OrderStatus status,
        BigDecimal total,
        LocalDateTime createdAt,
        List<OrderItemResponseDTO> items
) {
    public static OrderResponseDTO from(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponseDTO::from).toList()
                : List.of();

        return new OrderResponseDTO(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                itemDTOs
        );
    }
}
