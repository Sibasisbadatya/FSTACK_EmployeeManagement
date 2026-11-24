package com.example.emplyee_management.service;

import com.example.emplyee_management.dto.OrderItemRequestDTO;
import com.example.emplyee_management.dto.OrderItemResponseDTO;
import com.example.emplyee_management.dto.OrderRequestDTO;
import com.example.emplyee_management.dto.OrderResponseDTO;
import com.example.emplyee_management.entity.*;
import com.example.emplyee_management.exception.InsufficientStockException;
import com.example.emplyee_management.exception.InvalidOrderStateException;
import com.example.emplyee_management.exception.ResourceNotFoundException;
import com.example.emplyee_management.repository.*;
import com.example.emplyee_management.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Core Transactional Order Placement Workflow with Idempotency & Stock Concurrency Control
     */
    @Transactional
    public OrderResponseDTO placeOrder(String idempotencyKey, OrderRequestDTO request, Authentication authentication) {
        // ---------------------------------------------------------------------
        // Step A: Idempotency & Fingerprint Verification
        // ---------------------------------------------------------------------
        String fingerprint = HashUtils.sha256(request.toString());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyRecord> existingRecord = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
            if (existingRecord.isPresent()) {
                IdempotencyRecord record = existingRecord.get();

                // Check payload tampering
                if (!record.getRequestFingerprint().equals(fingerprint)) {
                    throw new IllegalArgumentException(
                            "Idempotency key '" + idempotencyKey + "' was already used with a different request payload!"
                    );
                }

                // If already completed, safely return the existing order
                if (record.getStatus() == IdempotencyStatus.COMPLETED && record.getOrder() != null) {
                    log.info("Idempotent hit: returning existing order id {}", record.getOrder().getId());
                    return OrderResponseDTO.from(record.getOrder());
                }

                if (record.getStatus() == IdempotencyStatus.STARTED) {
                    throw new IllegalStateException("A request with idempotency key '" + idempotencyKey + "' is already in progress.");
                }
            }
        }

        // ---------------------------------------------------------------------
        // Step 1: Validate Customer (Employee)
        // ---------------------------------------------------------------------
        Employee customer = employeeRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Customer (Employee) not found with id: " + request.customerId()));

        // ---------------------------------------------------------------------
        // Step 2 & 3: Lock Products & Validate Stock (sorted to prevent deadlocks)
        // ---------------------------------------------------------------------
        // Sort items by product ID to acquire pessimistic locks in a consistent order
        List<OrderItemRequestDTO> sortedItems = request.items().stream()
                .sorted(Comparator.comparing(OrderItemRequestDTO::productId))
                .toList();

        Map<Long, Product> lockedProducts = new LinkedHashMap<>();
        BigDecimal orderTotal = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemReq : sortedItems) {
            // Using Approach B: Pessimistic Row Lock (SELECT ... FOR UPDATE)
            Product product = productRepository.findByIdForUpdate(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.productId()));

            if (product.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName() + "'. Available: " +
                        product.getStock() + ", Requested: " + itemReq.quantity()
                );
            }

            // Calculate item price strictly from Database price
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            orderTotal = orderTotal.add(itemTotal);

            lockedProducts.put(product.getId(), product);
        }

        // ---------------------------------------------------------------------
        // Step 4 & 5: Create Order and Items
        // ---------------------------------------------------------------------
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PLACED)
                .total(orderTotal)
                .build();

        Order savedOrder = orderRepository.save(order);

        // ---------------------------------------------------------------------
        // Step 6, 7 & 8: Decrement Stock, Save Items & Record Inventory Tx
        // ---------------------------------------------------------------------
        for (OrderItemRequestDTO itemReq : sortedItems) {
            Product product = lockedProducts.get(itemReq.productId());

            // 1. Decrement stock
            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            // 2. Create Order Item
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            savedOrder.addItem(orderItem);

            // 3. Record Inventory Transaction (Audit trail)
            InventoryTransaction tx = InventoryTransaction.builder()
                    .product(product)
                    .order(savedOrder)
                    .transactionType(InventoryTransactionType.ORDER_PLACED)
                    .quantity(-itemReq.quantity()) // negative indicates stock reduction
                    .build();
            inventoryTransactionRepository.save(tx);
        }

        orderRepository.save(savedOrder);

        // ---------------------------------------------------------------------
        // Step 9: Save Idempotency Record
        // ---------------------------------------------------------------------
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .requestFingerprint(fingerprint)
                    .status(IdempotencyStatus.COMPLETED)
                    .order(savedOrder)
                    .build();
            idempotencyRecordRepository.save(record);
        }

        log.info("Successfully placed order id: {} for customer id: {}, total: {}",
                savedOrder.getId(), customer.getId(), orderTotal);

        return OrderResponseDTO.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderResponseDTO.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponseDTO::from)
                .toList();
    }

    /**
     * Order Cancellation with Pessimistic Locking & Stock Restoration
     */
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {
        // Lock the order row to serialize cancellation requests
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order #" + orderId + " has already been cancelled!");
        }

        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    "Order #" + orderId + " cannot be cancelled because current status is " + order.getStatus()
            );
        }

        // Restore stock for all items
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProduct().getId()));

            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);

            // Audit transaction
            InventoryTransaction tx = InventoryTransaction.builder()
                    .product(product)
                    .order(order)
                    .transactionType(InventoryTransactionType.ORDER_CANCELLED)
                    .quantity(item.getQuantity()) // positive indicates restored stock
                    .build();
            inventoryTransactionRepository.save(tx);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order id {} successfully cancelled and stock restored.", orderId);
        return OrderResponseDTO.from(updatedOrder);
    }
}
