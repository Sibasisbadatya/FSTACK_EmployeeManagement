package com.example.emplyee_management.service;

import com.example.emplyee_management.dto.EmployeeDTO;
import com.example.emplyee_management.dto.EmployeeResponseDTO;
import com.example.emplyee_management.entity.Department;
import com.example.emplyee_management.entity.Employee;
import com.example.emplyee_management.repository.DepartmentRepository;
import com.example.emplyee_management.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        // Remove emitter when client disconnects, times out, or errors
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    public void broadcastNewEmployee(Employee employee) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter
                                .event()
                                .name("new-employee")
                                .data(EmployeeResponseDTO.from(employee)));
            } catch (Exception e) {
                emitters.remove(emitter);
                throw new RuntimeException(e);
            }
        }
    }

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
        log.info("Department Found {}", department);
        Employee employee = employeeRepository.findByEmail(employeeDTO.getEmail()).orElseThrow(() -> new RuntimeException("No EMployee Found"));
        if (!employee.getVersion().equals(employeeDTO.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Employee has already been modified");
        }
        log.info("Employee Found {}", employee);
        employee.setName(employeeDTO.getName());
        employee.setDepartment(department);
        return employee;
    }

    @Transactional
    public Employee addEmployee(EmployeeDTO employeeDTO) {
        String deptName = employeeDTO.getDepartmentName();
        Department department = departmentRepository.findByName(deptName).orElseGet(() -> {
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

    public Page<Employee> getAllEmployee(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }
}
