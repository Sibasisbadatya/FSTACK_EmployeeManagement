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
    public ResponseEntity<GlobalResponseDTO<Map<String, String>>> handleMethodArguementException(MethodArgumentNotValidException ex) {
        log.info("Error in Controller {}", ex.getMessage());
        Map<String, String> fieldError = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach((error) -> {
                    fieldError.put(error.getField(), error.getDefaultMessage());
                });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponseDTO.of(HttpStatus.BAD_REQUEST, "Validation failed", fieldError));
    }

    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockingFailureException.class
    })
    public ResponseEntity<GlobalResponseDTO<Void>> handleOptimisticLock(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(GlobalResponseDTO.of(
                        HttpStatus.CONFLICT,
                        "Employee was modified by another user. Please refresh and try again.",
                        null
                ));
    }
}
