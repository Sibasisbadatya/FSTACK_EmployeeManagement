package com.example.emplyee_management.controller;

import com.example.emplyee_management.dto.GlobalResponseDTO;
import com.example.emplyee_management.dto.OrderRequestDTO;
import com.example.emplyee_management.dto.OrderResponseDTO;
import com.example.emplyee_management.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<GlobalResponseDTO<OrderResponseDTO>> placeOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderRequestDTO request, Authentication authentication) {
        OrderResponseDTO response = orderService.placeOrder(idempotencyKey, request,authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalResponseDTO.of(HttpStatus.CREATED, "Order placed successfully", response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponseDTO<List<OrderResponseDTO>>> getOrders(
            @RequestParam(value = "customerId", required = false) Long customerId) {
        if (customerId == null) {
            return ResponseEntity.badRequest()
                    .body(GlobalResponseDTO.of(HttpStatus.BAD_REQUEST, "customerId query parameter is required", null));
        }
        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(
                GlobalResponseDTO.of(HttpStatus.OK, "Orders retrieved successfully", orders)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponseDTO<OrderResponseDTO>> getOrderDetails(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(
                GlobalResponseDTO.of(HttpStatus.OK, "Order retrieved successfully", order)
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<GlobalResponseDTO<OrderResponseDTO>> cancelOrder(@PathVariable Long id) {
        OrderResponseDTO cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(
                GlobalResponseDTO.of(HttpStatus.OK, "Order cancelled successfully and stock restored", cancelledOrder)
        );
    }
}
