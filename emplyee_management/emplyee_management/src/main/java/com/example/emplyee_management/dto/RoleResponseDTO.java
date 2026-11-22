package com.example.emplyee_management.dto;

import com.example.emplyee_management.entity.Role;

public record RoleResponseDTO(Long id, String name) {
    public static RoleResponseDTO from(Role role) {
        return new RoleResponseDTO(role.getId(), role.getName());
    }
}
