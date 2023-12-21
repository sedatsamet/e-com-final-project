package org.sedatsamet.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.authservice.dto.UserLoginRequest;
import org.sedatsamet.authservice.entity.User;
import org.sedatsamet.authservice.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthUtil authUtil;

    /**
     * Generate a JWT token for the given user login request.
     *
     * @param userLoginRequest The user login request containing username and password.
     * @return Generated JWT token upon successful authentication, or null if authentication fails.
     */
    public String generateToken(UserLoginRequest userLoginRequest) {
        try {
            User registeredUser = authUtil.getLoggedInUserDetailsByUsername(userLoginRequest.getUsername());
            Authentication authenticate = new UsernamePasswordAuthenticationToken(
                    registeredUser,
                    userLoginRequest.getPassword(),
                    registeredUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticate);
            if (authenticate.isAuthenticated()) {
                return jwtService.generateToken(userLoginRequest.getUsername());
            } else {
                return null;
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error generating token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Validate the given JWT token.
     *
     * @param token The JWT token to validate.
     * @return A string indicating whether the token is valid or not.
     */
    public String validateToken(String token) {
        try {
            return jwtService.validateToken(token) ? "Token is valid" : "Token is not valid";
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error validating token: {}", e.getMessage(), e);
            return "Error validating token";
        }
    }
}
