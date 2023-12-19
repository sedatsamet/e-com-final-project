package org.sedatsamet.authservice.service;

import org.sedatsamet.authservice.dto.UserLoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    public String generateToken(UserLoginRequest userLoginRequest) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequest.getUsername(),
                        userLoginRequest.getPassword()));
        if (authenticate.isAuthenticated()) {
            String generatedToken = jwtService.generateToken(userLoginRequest.getUsername());
            return generatedToken;
        } else {
            return null;
        }
    }

    public String validateToken(String token) {
        return jwtService.validateToken(token) ? "Token is valid" : "Token is not valid";
    }
}
