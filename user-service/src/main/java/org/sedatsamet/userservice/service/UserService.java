package org.sedatsamet.userservice.service;


import org.sedatsamet.userservice.dto.request.UserCreateRequest;
import org.sedatsamet.userservice.dto.request.UserUpdateRequest;
import org.sedatsamet.userservice.entity.User;
import org.sedatsamet.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
                .orderIdList(new ArrayList<>())
                .accountNonExpired(true)
                .isEnabled(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        userRepository.save(newUser);
        return newUser;
    }

    public User updateUser(UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();
        if(authenticatedUser.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN")) {
            return userUpdate(request);
        }else{
            if(authenticatedUser.getUserId().equals(request.getUserId())) {
                return userUpdate(request);
            }else{
                throw new RuntimeException("You are not authorized to view this user");
            }
        }
    }

    public User getUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();
        if(authenticatedUser.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN")) {
            return userRepository.findById(userId).orElse(null);
        }else{
            if(authenticatedUser.getUserId().equals(userId)) {
                return userRepository.findById(userId).orElse(null);
            }else{
                throw new RuntimeException("You are not authorized to view this user");
            }
        }
    }

    public User getUserByUserName(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    private User userUpdate(UserUpdateRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if(user != null) {
            user.setName(request.getName());
            user.setSurName(request.getSurName());
            user.setTelephone(request.getTelephone());
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAuthorities(request.getAuthorities());
            user.setOrderIdList(request.getOrderIdList());
            userRepository.saveAndFlush(user);
        }
        return user;
    }
}
