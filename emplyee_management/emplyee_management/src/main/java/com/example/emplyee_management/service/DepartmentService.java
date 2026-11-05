package com.example.emplyee_management.service;


import com.example.emplyee_management.dto.DepartmentDTO;
import com.example.emplyee_management.entity.Department;
import com.example.emplyee_management.mapper.DepartmentMapper;
import com.example.emplyee_management.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public Department saveDepartment(DepartmentDTO departmentDTO) {
        Department department = departmentMapper.toEntity(departmentDTO);
        return departmentRepository.save(department);
    }

}
