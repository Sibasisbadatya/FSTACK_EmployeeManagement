package com.example.emplyee_management.controller;


import com.example.emplyee_management.dto.RoleDTO;
import com.example.emplyee_management.entity.Role;
import com.example.emplyee_management.service.RolesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    private final RolesService rolesService;

    public RoleController(RolesService rolesService) {
        this.rolesService = rolesService;
    }

    @PostMapping
    public ResponseEntity<Role> addRole(@Valid @RequestBody RoleDTO roleDTO){
        Role role =rolesService.addRoles(roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

}
