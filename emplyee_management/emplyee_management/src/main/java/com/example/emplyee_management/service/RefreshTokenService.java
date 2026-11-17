package com.example.emplyee_management.service;

import com.example.emplyee_management.entity.Employee;
import com.example.emplyee_management.entity.RefreshToken;
import com.example.emplyee_management.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Objects;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import static java.util.Objects.hash;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshTokenExpiration;


    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, @Value("${refresh-token.expiration}") long refreshTokenExpiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    @Transactional
    public String createRefreshToken(Employee user) {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        String tokenHash =
                hashToken(rawToken);

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setTokenHash(tokenHash);

        refreshToken.setUser(user);

        refreshToken.setExpiryDate(
                Instant.now()
                        .plusSeconds(
                                refreshTokenExpiration
                        )
        );

        refreshToken.setRevoked(false);

        refreshTokenRepository.save(
                refreshToken
        );

        /*
         * IMPORTANT:
         *
         * Return RAW token to client.
         *
         * Database only contains the hash.
         */

        return rawToken;
    }
    public RefreshToken verifyRefreshToken(
            String rawToken) {

        String tokenHash =
                hashToken(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.isRevoked()) {

            throw new RuntimeException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken
                .getExpiryDate()
                .isBefore(Instant.now())) {

            throw new RuntimeException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }


    // ==========================================
    // REVOKE TOKEN
    // ==========================================

    @Transactional
    public void revokeToken(
            RefreshToken refreshToken) {

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(
                refreshToken
        );
    }


    // ==========================================
    // HASH TOKEN
    // ==========================================

    private String hashToken(
            String rawToken) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (
                NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 is not available",
                    e
            );
        }
    }

    @Transactional
    public void logout(
            String rawRefreshToken) {

        RefreshToken refreshToken =
                verifyRefreshToken(
                        rawRefreshToken
                );

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(
                refreshToken
        );
    }

    @Transactional
    public void revokeRawToken(
            String rawToken) {

        String tokenHash =
                hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(token -> {

                    token.setRevoked(true);

                    refreshTokenRepository.save(
                            token
                    );
                });
    }

}
