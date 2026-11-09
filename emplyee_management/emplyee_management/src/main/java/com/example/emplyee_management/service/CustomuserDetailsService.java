package com.example.emplyee_management.service;

import com.example.emplyee_management.entity.CustomeUserDetails;
import com.example.emplyee_management.entity.Employee;
import com.example.emplyee_management.repository.EmployeeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomuserDetailsService implements UserDetailsService {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public CustomuserDetailsService(EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(username).orElseThrow(()->new RuntimeException("User Not Found"));
        return new CustomeUserDetails(employee);
    }
}
