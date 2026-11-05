package com.example.emplyee_management.controller;


import com.example.emplyee_management.dto.DepartmentDTO;
import com.example.emplyee_management.entity.Department;
import com.example.emplyee_management.service.DepartmentService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Department> createDepartment(@RequestBody @Valid DepartmentDTO department){
        Department savedDepartment =  departmentService.saveDepartment(department);
        return ResponseEntity
                .status(201)
                .body(savedDepartment);
    }

}
