package org.sedatsamet.authservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    /**
     * Validates the given JWT token.
     * This method attempts to parse and validate the token using the signing key.
     *
     * @param token The JWT token to be validated.
     * @return True if the token is valid; false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Log a warning message and return false if token validation fails
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generates a JWT token for the provided username.
     * This method creates a token with the given claims and username.
     *
     * @param userName The username for which the token is generated.
     * @return The generated JWT token.
     * @throws RuntimeException If there's an issue creating the token.
     */
    public String generateToken(String userName) {
        try {
            Map<String, Object> claims = new HashMap<>();
            return createToken(claims, userName);
        } catch (Exception e) {
            // Log the exception for debugging purposes and throw a runtime exception
            log.error("Error generating token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    /**
     * Creates a JWT token with the provided claims and username.
     *
     * @param claims   The claims to be included in the token.
     * @param userName The username for which the token is created.
     * @return The generated JWT token.
     * @throws RuntimeException If there's an issue creating the token.
     */
    private String createToken(Map<String, Object> claims, String userName) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userName)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            // Log the exception for debugging purposes and throw a runtime exception
            log.error("Error creating token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create token", e);
        }
    }

    /**
     * Retrieves the signing key used for JWT operations.
     *
     * @return The signing key.
     * @throws RuntimeException If there's an issue decoding or generating the signing key.
     */
    private Key getSignKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            // Log the exception for debugging purposes and throw a runtime exception
            log.error("Error generating signing key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate signing key", e);
        }
    }
}
