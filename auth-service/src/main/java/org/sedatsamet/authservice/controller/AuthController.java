package org.sedatsamet.authservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.authservice.dto.UserLoginRequest;
import org.sedatsamet.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * Endpoint to authenticate a user and generate a JWT token upon successful authentication.
     *
     * @param userLoginRequest The user login request containing username and password.
     * @return ResponseEntity containing the generated JWT token upon successful authentication,
     *         or a NOT_FOUND status with an error message if the authentication fails.
     */
    @PostMapping("/login")
    public ResponseEntity<String> logIn(@RequestBody UserLoginRequest userLoginRequest) {
        try {
            log.info("User login request: {}", userLoginRequest);
            String token = authService.generateToken(userLoginRequest);
            if (token != null) {
                log.info("User logged in successfully with token: {}", token);
                return ResponseEntity.ok(token);
            } else {
                log.error("Invalid username or password");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid username or password");
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error during user login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during user login.");
        }
    }

    /**
     * Endpoint to validate a JWT token.
     * Checks the validity of the provided token using the authentication service.
     *
     * @param token The JWT token to be validated.
     * @return ResponseEntity indicating whether the token is valid or not.
     *         Returns a success response if the token is valid,
     *         or an unauthorized status with an error message if the token is invalid.
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            String response = authService.validateToken(token);
            if ("Token is valid".equals(response)) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error validating token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error validating token.");
        }
    }
}
