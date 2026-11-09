package com.example.emplyee_management.service;

import com.example.emplyee_management.dto.RoleDTO;
import com.example.emplyee_management.entity.Role;
import com.example.emplyee_management.entity.Role;
import com.example.emplyee_management.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RolesService {

    private final RoleRepository roleRepository;

    public RolesService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role addRoles(RoleDTO roleDto) {
        Role role = new Role();
        role.setName(roleDto.getName());
        return roleRepository.save(role);
    }

}
