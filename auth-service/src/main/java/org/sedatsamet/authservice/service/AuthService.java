package org.sedatsamet.authservice.service;

import org.sedatsamet.authservice.dto.UserLoginRequest;
import org.sedatsamet.authservice.entity.User;
import org.sedatsamet.authservice.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthUtil authUtil;

    public String generateToken(UserLoginRequest userLoginRequest) {
        User registeredUser = authUtil.getLoggedInUserDetailsByUsername(userLoginRequest.getUsername());
        Authentication authenticate = new UsernamePasswordAuthenticationToken(
                registeredUser,
                userLoginRequest.getPassword(),
                registeredUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticate);
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
