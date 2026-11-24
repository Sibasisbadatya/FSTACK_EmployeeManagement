package com.example.emplyee_management.service;

import com.example.emplyee_management.dto.ProductRequestDTO;
import com.example.emplyee_management.dto.ProductResponseDTO;
import com.example.emplyee_management.dto.StockAdjustmentDTO;
import com.example.emplyee_management.entity.InventoryTransaction;
import com.example.emplyee_management.entity.InventoryTransactionType;
import com.example.emplyee_management.entity.Product;
import com.example.emplyee_management.exception.InsufficientStockException;
import com.example.emplyee_management.exception.ResourceNotFoundException;
import com.example.emplyee_management.repository.InventoryTransactionRepository;
import com.example.emplyee_management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("Product with SKU '" + request.sku() + "' already exists");
        }

        Product product = Product.builder()
                .sku(request.sku().trim().toUpperCase())
                .name(request.name().trim())
                .price(request.price())
                .stock(request.stock())
                .build();

        Product savedProduct = productRepository.save(product);

        // Record initial inventory transaction if stock > 0
        if (savedProduct.getStock() > 0) {
            InventoryTransaction tx = InventoryTransaction.builder()
                    .product(savedProduct)
                    .transactionType(InventoryTransactionType.STOCK_ADDED)
                    .quantity(savedProduct.getStock())
                    .build();
            inventoryTransactionRepository.save(tx);
        }

        log.info("Created product with id: {}, SKU: {}", savedProduct.getId(), savedProduct.getSku());
        return ProductResponseDTO.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ProductResponseDTO.from(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(request.name().trim());
        product.setPrice(request.price());

        Product updated = productRepository.save(product);
        return ProductResponseDTO.from(updated);
    }

    @Transactional
    public ProductResponseDTO adjustStock(Long id, StockAdjustmentDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        int newStock = product.getStock() + request.quantityChange();
        if (newStock < 0) {
            throw new InsufficientStockException(
                    "Cannot adjust stock. Current stock is " + product.getStock() +
                    ", requested reduction is " + Math.abs(request.quantityChange())
            );
        }

        product.setStock(newStock);
        Product savedProduct = productRepository.save(product);

        // Determine transaction type
        InventoryTransactionType txType = request.quantityChange() > 0
                ? InventoryTransactionType.STOCK_ADDED
                : InventoryTransactionType.STOCK_ADJUSTED;

        InventoryTransaction tx = InventoryTransaction.builder()
                .product(savedProduct)
                .transactionType(txType)
                .quantity(request.quantityChange())
                .build();
        inventoryTransactionRepository.save(tx);

        log.info("Adjusted stock for product {}: new stock = {}", id, newStock);
        return ProductResponseDTO.from(savedProduct);
    }
}
