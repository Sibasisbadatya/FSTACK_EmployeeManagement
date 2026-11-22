package com.example.emplyee_management.dto;

import com.example.emplyee_management.entity.Employee;

import java.util.List;

public record EmployeeResponseDTO(
        Long id,
        String name,
        String email,
        Long version,
        DepartmentResponseDTO department,
        List<RoleResponseDTO> roles
) {
    public static EmployeeResponseDTO from(Employee employee) {
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getVersion(),
                DepartmentResponseDTO.from(employee.getDepartment()),
                employee.getRoles().stream()
                        .map(RoleResponseDTO::from)
                        .toList()
        );
    }
}
