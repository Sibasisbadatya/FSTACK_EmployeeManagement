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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;


    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public Employee updateEmployee(EmployeeDTO employeeDTO) {
        System.out.println("Sibasis Badatya");
        String deptName = employeeDTO.getDepartmentName();
        Department department = departmentRepository.findByName(deptName).orElseThrow(() -> new RuntimeException("No Department Found"));
        log.info("Department Found {}",department);
        Employee employee = employeeRepository.findByEmail(employeeDTO.getEmail()).orElseThrow(()->new RuntimeException("No EMployee Found"));
        log.info("Employee Found {}",employee);
        employee.setName(employeeDTO.getName());
        employee.setDepartment(department);
        return employee;
    }

    @Transactional
    public Employee addEmployee(EmployeeDTO employeeDTO) {
        String deptName = employeeDTO.getDepartmentName();
        Department department = departmentRepository.findByName(deptName).orElseGet(()->{
           Department newDepartment = new Department();
            newDepartment.setName(deptName);
           return newDepartment;
        });
        Employee employee = new Employee();
        employee.setEmail(employeeDTO.getEmail());
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        employee.setName(employeeDTO.getName());
        employee.setDepartment(department);
        return employeeRepository.save(employee);
    }

    public Page<Employee> getAllEmployee(Pageable pageable){
        return employeeRepository.findAll(pageable);
    }
}
