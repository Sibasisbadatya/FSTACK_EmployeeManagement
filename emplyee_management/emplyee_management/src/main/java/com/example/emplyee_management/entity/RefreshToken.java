package com.example.emplyee_management.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenHash;

    private Instant expiryDate;

    private boolean revoked;

    @ManyToOne
    private Employee user;

    public boolean isRevoked() {
        return revoked;
    }
}
