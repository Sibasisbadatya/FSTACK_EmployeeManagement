package com.example.emplyee_management.controller;


import com.example.emplyee_management.dto.DepartmentDTO;
import com.example.emplyee_management.dto.DepartmentResponseDTO;
import com.example.emplyee_management.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/department")
@RestController
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> createDepartment(@RequestBody @Valid DepartmentDTO department){
        DepartmentResponseDTO savedDepartment = DepartmentResponseDTO.from(departmentService.saveDepartment(department));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDepartment);
    }

}
