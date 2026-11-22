package com.example.emplyee_management.controller;


import com.example.emplyee_management.dto.EmployeeDTO;
import com.example.emplyee_management.dto.EmployeeResponseDTO;
import com.example.emplyee_management.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/addEmployee")
    public ResponseEntity<EmployeeResponseDTO> addEmployee(@RequestBody @Valid EmployeeDTO employee) {
        EmployeeResponseDTO savedEmployee = EmployeeResponseDTO.from(employeeService.addEmployee(employee));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
    }

    @PutMapping("/updateEmployee")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@RequestBody @Valid EmployeeDTO employee) {
        EmployeeResponseDTO updatedEmployee = EmployeeResponseDTO.from(employeeService.updateEmployee(employee));
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedEmployee);
    }


    @GetMapping("/getAllEmployee")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployee(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmployeeResponseDTO> employees = employeeService.getAllEmployee(pageable)
                .map(EmployeeResponseDTO::from);
        return ResponseEntity.ok().body(employees);
    }

    @GetMapping(value = "/stream/getAllEmployee",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Page<EmployeeResponseDTO>> streamAllEmployee(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmployeeResponseDTO> employees = employeeService.getAllEmployee(pageable)
                .map(EmployeeResponseDTO::from);
        return ResponseEntity.ok().body(employees);
    }

}
