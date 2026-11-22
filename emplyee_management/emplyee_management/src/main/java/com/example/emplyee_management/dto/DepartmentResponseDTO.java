package com.example.emplyee_management.dto;

import com.example.emplyee_management.entity.Department;

public record DepartmentResponseDTO(Long id, String name) {
    public static DepartmentResponseDTO from(Department department) {
        return new DepartmentResponseDTO(department.getId(), department.getName());
    }
}
