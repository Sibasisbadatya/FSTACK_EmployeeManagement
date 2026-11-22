package com.example.emplyee_management.controller;

import com.example.emplyee_management.dto.LoginRequestDTO;
import com.example.emplyee_management.dto.LoginResponseDTO;
import com.example.emplyee_management.dto.RefreshRequest;
import com.example.emplyee_management.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginDTO) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginDTO.getEmail(),
                loginDTO.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        return ResponseEntity.ok(authService.login(authentication));
    }

    @PostMapping("/refresh")
    public LoginResponseDTO refresh(
            @RequestBody RefreshRequest request) {

        return authService.refresh(request.getRefreshToken());
    }
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(
            @RequestBody RefreshRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(true);
    }
}
