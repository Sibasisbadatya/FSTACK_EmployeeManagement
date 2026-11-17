package com.example.emplyee_management.service;

import com.example.emplyee_management.dto.LoginResponseDTO;
import com.example.emplyee_management.entity.Employee;
import com.example.emplyee_management.entity.RefreshToken;
import com.example.emplyee_management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuthService {
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Long expiry;
    private final RefreshTokenService refreshTokenService;
    private final EmployeeRepository employeeRepository;
    private final UserDetailsService userDetailsService;

    public AuthService(
            JwtEncoder jwtEncoder,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expiry}") Long expiry,
            RefreshTokenService refreshTokenService,
            EmployeeRepository employeeRepository,
            UserDetailsService userDetailsService) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expiry = expiry;
        this.refreshTokenService = refreshTokenService;
        this.employeeRepository = employeeRepository;
        this.userDetailsService = userDetailsService;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        List<String> authorities =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));
        return jwt.getTokenValue();
    }

    @Transactional
    public LoginResponseDTO login(Authentication authentication) {
        Employee employee = employeeRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found"));

        return new LoginResponseDTO(
                generateToken(authentication),
                refreshTokenService.createRefreshToken(employee)
        );
    }

    @Transactional
    public LoginResponseDTO refresh(String rawRefreshToken) {
        RefreshToken oldRefreshToken =
                refreshTokenService.verifyRefreshToken(rawRefreshToken);
        Employee employee = oldRefreshToken.getUser();

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(employee.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        refreshTokenService.revokeToken(oldRefreshToken);

        String newAccessToken = generateToken(authentication);
        String newRefreshToken = refreshTokenService.createRefreshToken(employee);
        return new LoginResponseDTO(newAccessToken, newRefreshToken);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.logout(rawRefreshToken);
    }
}
