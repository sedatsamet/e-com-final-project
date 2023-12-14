package org.sedatsamet.userservice.service;


import org.sedatsamet.userservice.dto.request.UserCreateRequest;
import org.sedatsamet.userservice.entity.User;
import org.sedatsamet.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(UserCreateRequest request) {
        User newUser = User.builder()
                .name(request.getName())
                .surName(request.getSurName())
                .telephone(request.getTelephone())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .authorities(request.getAuthorities())
                .cartId(UUID.randomUUID())
                .orderIdList(List.of())
                .accountNonExpired(true)
                .isEnabled(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        userRepository.save(newUser);

        return newUser;
    }

}
