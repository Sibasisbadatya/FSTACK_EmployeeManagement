package com.example.emplyee_management.controller;

import com.example.emplyee_management.dto.GlobalResponseDTO;
import com.example.emplyee_management.dto.ProductRequestDTO;
import com.example.emplyee_management.dto.ProductResponseDTO;
import com.example.emplyee_management.dto.StockAdjustmentDTO;
import com.example.emplyee_management.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<GlobalResponseDTO<ProductResponseDTO>> createProduct(
            @Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalResponseDTO.of(HttpStatus.CREATED, "Product created successfully", created));
    }

    @GetMapping
    public ResponseEntity<GlobalResponseDTO<List<ProductResponseDTO>>> listProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(
                GlobalResponseDTO.of(HttpStatus.OK, "Products retrieved successfully", products)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponseDTO<ProductResponseDTO>> getProductDetails(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(
                GlobalResponseDTO.of(HttpStatus.OK, "Product retrieved successfully", product)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponseDTO<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                GlobalResponseDTO.of(HttpStatus.OK, "Product updated successfully", updated)
        );
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<GlobalResponseDTO<ProductResponseDTO>> adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustmentDTO request) {
        ProductResponseDTO updated = productService.adjustStock(id, request);
        return ResponseEntity.ok(
                GlobalResponseDTO.of(HttpStatus.OK, "Stock adjusted successfully", updated)
        );
    }
}
