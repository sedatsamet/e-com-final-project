package org.sedatsamet.userservice.service;


import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Retrieves a list of all users from the repository.
     *
     * @return List of User objects.
     * @throws RuntimeException If there's an issue retrieving the list of users.
     */
    public List<User> getAllUsers() {
        try {
            // Fetch and return all users from the userRepository
            return userRepository.findAll();
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error retrieving all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users");
        }
    }

    /**
     * Creates a new user based on the provided UserCreateRequest.
     *
     * @param request UserCreateRequest object containing user details.
     * @return The newly created User object.
     * @throws RuntimeException If there's an issue saving the user to the repository.
     */
    public User createUser(UserCreateRequest request) {
        try {
            // Construct a new User object with the provided request details
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

            // Save the new user to the userRepository
            userRepository.save(newUser);

            return newUser;
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user");
        }
    }

    /**
     * Updates a user based on the provided UserUpdateRequest.
     *
     * @param request UserUpdateRequest object containing user details to be updated.
     * @return The updated User object.
     * @throws RuntimeException If there's an issue updating the user or if the authenticated user is not authorized.
     */
    public User updateUser(UserUpdateRequest request) {
        try {
            // Retrieve the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User authenticatedUser = (User) authentication.getPrincipal();

            // Check if the authenticated user has ADMIN role or if they're updating their own profile
            if (authenticatedUser.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN") ||
                    authenticatedUser.getUserId().equals(request.getUserId())) {
                return userUpdate(request);
            } else {
                // Throw an exception if the authenticated user is not authorized to perform the update
                throw new RuntimeException("You are not authorized to update this user");
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error updating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user");
        }
    }

    /**
     * Retrieves a user based on the provided user ID, taking into consideration the current authenticated user's permissions.
     *
     * @param userId The ID of the user to retrieve.
     * @return The User object corresponding to the provided user ID.
     * @throws RuntimeException If there's an issue retrieving the user or if the authenticated user is not authorized.
     */
    public User getUser(UUID userId) {
        try {
            // Retrieve the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User authenticatedUser = (User) authentication.getPrincipal();

            // Check if the authenticated user has ADMIN role or if they're trying to view their own profile
            if (authenticatedUser.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN") ||
                    authenticatedUser.getUserId().equals(userId)) {
                return userRepository.findById(userId).orElse(null);
            } else {
                // Throw an exception if the authenticated user is not authorized to view the requested user
                throw new RuntimeException("You are not authorized to view this user");
            }
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error retrieving user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user");
        }
    }

    /**
     * Retrieves a user based on the provided username.
     *
     * @param username The username of the user to retrieve.
     * @return The User object corresponding to the provided username, or null if not found.
     * @throws RuntimeException If there's an issue retrieving the user.
     */
    public User getUserByUserName(String username) {
        try {
            // Retrieve the user by username from the repository
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error retrieving user by username: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user by username");
        }
    }

    /**
     * Updates the details of a user based on the provided update request.
     *
     * @param request The UserUpdateRequest containing the updated user details.
     * @return The updated User object, or null if the user was not found.
     * @throws RuntimeException If there's an issue updating the user details.
     */
    private User userUpdate(UserUpdateRequest request) {
        try {
            // Retrieve the user by ID from the repository
            User user = userRepository.findById(request.getUserId()).orElse(null);

            if(user != null) {
                // Update the user details
                user.setName(request.getName());
                user.setSurName(request.getSurName());
                user.setTelephone(request.getTelephone());
                user.setUsername(request.getUsername());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setAuthorities(request.getAuthorities());
                user.setOrderIdList(request.getOrderIdList());

                // Save the updated user details
                userRepository.saveAndFlush(user);
            }

            return user;
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error updating user details: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user details");
        }
    }
}
