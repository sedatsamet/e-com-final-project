package org.sedatsamet.authservice.service;

import org.sedatsamet.authservice.entity.User;
import org.sedatsamet.authservice.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = authRepository.findByUsername(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
    }

    public String generateToken(String userName) {
        return jwtService.generateToken(userName);
    }

    public String validateToken(String token) {
        return jwtService.validateToken(token) ? "Token is valid" : "Token is not valid";
    }
}
