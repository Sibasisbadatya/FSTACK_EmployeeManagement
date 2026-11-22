package com.example.emplyee_management.dto;

import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public record GlobalResponseDTO<T>(
        LocalDateTime timestamp,
        int statusCode,
        boolean success,
        String message,
        T data
) {
    public static <T> GlobalResponseDTO<T> of(HttpStatusCode status, String message, T data) {
        return new GlobalResponseDTO<>(
                LocalDateTime.now(),
                status.value(),
                status.is2xxSuccessful(),
                message,
                data
        );
    }
}
