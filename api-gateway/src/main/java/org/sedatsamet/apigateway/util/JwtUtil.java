package org.sedatsamet.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    /**
     * Validates the given JWT token using the configured signing key.
     * This method parses the token claims to ensure its validity.
     *
     * @param token The JWT token to be validated.
     * @throws JwtException If the token is invalid or cannot be parsed.
     */
    public void validateToken(String token) {
        try {
            log.info("Validating token in JwtUtil : {}", token);
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            log.info("Token is valid in JwtUtil : {}", token);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error validating token: {}", e.getMessage(), e);
            throw new RuntimeException("Token validation failed", e);
        }
    }

    /**
     * Extract the username from the given JWT token.
     *
     * @param token The JWT token from which to extract the username.
     * @return The username extracted from the token.
     */
    public String extractUserNameFromToken(String token) {
        try {
            log.info("Extracting user name from token : {}", token);
            Claims claims = parseTokenClaims(token);
            log.info("Extracted user name from token : {}", claims.getSubject());
            return claims.getSubject();
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error extracting username from token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract username from token", e);
        }
    }
    /**
     * Parses the provided JWT token to extract its claims.
     * This method verifies the signature of the token and then retrieves the body (claims) from it.
     *
     * @param token The JWT token that needs to be parsed to extract claims.
     * @return The parsed claims from the token.
     * @throws JwtException If the token is invalid or the claims cannot be parsed.
     */
    private Claims parseTokenClaims(String token) {
        try {
            log.info("Parsing token claims in JwtUtil : {}", token);
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error parsing token claims: {}", e.getMessage(), e);
            throw new JwtException("Failed to parse token claims", e);
        }
    }

    /**
     * Retrieves the signing key used for JWT token validation.
     * This method decodes the base64 encoded secret key and creates a HMAC SHA key.
     *
     * @return The signing key for JWT token validation.
     * @throws RuntimeException If an error occurs while generating the signing key.
     */
    private Key getSignKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error generating signing key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate signing key", e);
        }
    }
}
