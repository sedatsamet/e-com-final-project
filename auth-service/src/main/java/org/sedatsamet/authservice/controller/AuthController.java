package org.sedatsamet.authservice.controller;

import org.sedatsamet.authservice.dto.UserLoginRequest;
import org.sedatsamet.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<String> logIn(@RequestBody UserLoginRequest userLoginRequest) {
        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        userLoginRequest.getUsername(),
                        userLoginRequest.getPassword()));
        if(authenticate.isAuthenticated()){
            return ResponseEntity.ok(authService.generateToken(userLoginRequest.getUsername()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid username or password");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        String response = authService.validateToken(token);
        return response == "Token is valid" ? ResponseEntity.ok(response) : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
