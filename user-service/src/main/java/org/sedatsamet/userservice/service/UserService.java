package org.sedatsamet.userservice.service;


import org.sedatsamet.userservice.dto.request.UserCreateRequest;
import org.sedatsamet.userservice.dto.request.UserUpdateRequest;
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

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

    public User updateUser(UserUpdateRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if(user != null) {
            user.setName(request.getName());
            user.setSurName(request.getSurName());
            user.setTelephone(request.getTelephone());
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAuthorities(request.getAuthorities());
            userRepository.save(user);
        }
        return user;
    }

    public User getUser(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User getUserByUserName(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
