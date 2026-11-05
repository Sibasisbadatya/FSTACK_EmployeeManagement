package com.example.emplyee_management.mapper;

import com.example.emplyee_management.dto.DepartmentDTO;
import com.example.emplyee_management.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id",ignore = true)
    Department toEntity(DepartmentDTO departmentDTO);

    DepartmentDTO toDto(Department department);

}
