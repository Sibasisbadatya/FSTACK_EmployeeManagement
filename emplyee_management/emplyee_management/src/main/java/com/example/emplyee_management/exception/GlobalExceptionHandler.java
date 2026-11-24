package com.example.emplyee_management.exception;

import com.example.emplyee_management.dto.GlobalResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponseDTO<Map<String, String>>> handleMethodArgumentException(MethodArgumentNotValidException ex) {
        log.info("Validation error in Controller: {}", ex.getMessage());
        Map<String, String> fieldError = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach((error) -> {
                    fieldError.put(error.getField(), error.getDefaultMessage());
                });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponseDTO.of(HttpStatus.BAD_REQUEST, "Validation failed", fieldError));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalResponseDTO<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalResponseDTO.of(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<GlobalResponseDTO<Void>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Insufficient stock error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(GlobalResponseDTO.of(HttpStatus.CONFLICT, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<GlobalResponseDTO<Void>> handleInvalidOrderState(InvalidOrderStateException ex) {
        log.warn("Invalid order state transition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponseDTO.of(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponseDTO.of(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockingFailureException.class
    })
    public ResponseEntity<GlobalResponseDTO<Void>> handleOptimisticLock(Exception ex) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(GlobalResponseDTO.of(
                        HttpStatus.CONFLICT,
                        "Resource was modified by another transaction concurrently. Please refresh and retry.",
                        null
                ));
    }
}
