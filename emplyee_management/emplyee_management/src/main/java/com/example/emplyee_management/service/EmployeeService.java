package com.example.emplyee_management.service;

import com.example.emplyee_management.dto.EmployeeDTO;
import com.example.emplyee_management.entity.Department;
import com.example.emplyee_management.entity.Employee;
import com.example.emplyee_management.repository.DepartmentRepository;
import com.example.emplyee_management.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;


    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }


    @Transactional
    public Employee updateEmployee(EmployeeDTO employeeDTO) {
        System.out.println("Sibasis Badatya");
        Long deptId = employeeDTO.getDepartmentId();
        Department department = departmentRepository.findById(deptId).orElseThrow(() -> new RuntimeException("No Department Found"));
        log.info("Department Found {}",department);
        Employee employee = employeeRepository.findByEmail(employeeDTO.getEmail()).orElseThrow(()->new RuntimeException("No EMployee Found"));
        log.info("Employee Found {}",employee);
        employee.setName(employeeDTO.getName());
        employee.setDepartment(department);
        return employee;
    }

    public Employee addEmployee(EmployeeDTO employeeDTO) {
        Long deptId = employeeDTO.getDepartmentId();
        Department department = departmentRepository.findById(deptId).orElseThrow(() -> new RuntimeException("No Department Found"));
        Employee employee = new Employee();
        employee.setEmail(employeeDTO.getEmail());
        employee.setName(employeeDTO.getName());
        return employeeRepository.save(employee);
    }

    public Page<Employee> getAllEmployee(Pageable pageable){
        return employeeRepository.findAll(pageable);
    }
}
