package com.example.emplyee_management.exception;

import com.example.emplyee_management.dto.ValidationExceptionResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationExceptionResponseDTO> handleMethodArguementException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.info("Error in Controller {}", ex.getMessage());
        Map<String, String> fieldError = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach((error) -> {
                    fieldError.put(error.getField(), error.getDefaultMessage());
                });

        ValidationExceptionResponseDTO dto = new ValidationExceptionResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                request.getRequestURI(),
                fieldError
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(dto);
    }

}
