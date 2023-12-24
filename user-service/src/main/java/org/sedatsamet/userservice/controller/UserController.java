package org.sedatsamet.userservice.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.userservice.dto.request.UserCreateRequest;
import org.sedatsamet.userservice.dto.request.UserUpdateRequest;
import org.sedatsamet.userservice.entity.User;
import org.sedatsamet.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * Endpoint to retrieve a list of all users.
     *
     * @return List of User objects representing all users.
     * @throws RuntimeException If there's an issue retrieving the user list.
     */
    @GetMapping("/allUsers")
    public List<User> listAllUsers() {
        try {
            // Fetch the list of all users using the userService
            return userService.getAllUsers();
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic message
            log.error("Error fetching list of all users: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving user list");
        }
    }

    /**
     * Endpoint to register a new user.
     *
     * @param request UserCreateRequest object containing details of the new user to be registered.
     * @return ResponseEntity containing the newly created User object.
     * @throws RuntimeException If there's an issue creating the user.
     */
    @PostMapping("/register")
    public ResponseEntity<User> createNewUser(@RequestBody UserCreateRequest request) {
        try {
            // Create a new user using the userService and return it in the ResponseEntity
            return ResponseEntity.ok().body(userService.createUser(request));
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic message
            log.error("Error creating new user: {}", e.getMessage(), e);
            throw new RuntimeException("Error registering new user");
        }
    }

    /**
     * Endpoint to update an existing user's details.
     *
     * @param request UserUpdateRequest object containing the updated details of the user.
     * @return ResponseEntity containing the updated User object.
     * @throws RuntimeException If the user is not authorized to perform the update.
     */
    @PutMapping("/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequest request) {
        try {
            // Update the user using the userService and return the updated user in the ResponseEntity
            return ResponseEntity.ok().body(userService.updateUser(request));
        } catch (Exception e) {
            // Log the error and return an unauthorized response
            log.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to update this user");
        }
    }

    /**
     * Endpoint to retrieve details of a specific user based on the provided user ID.
     *
     * @param userId The UUID of the user to retrieve.
     * @return ResponseEntity containing the User object or an unauthorized message if the user is not authorized.
     * @throws RuntimeException If there's an issue retrieving the user or if the user is not authorized.
     */
    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(@RequestParam String userId) {
        try {
            // Retrieve the user using the userService and return it in the ResponseEntity
            return ResponseEntity.ok().body(userService.getUser(UUID.fromString(userId)));
        } catch (Exception e) {
            // Log the error and return an unauthorized response
            log.error("Error retrieving user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to view this user");
        }
    }

    /**
     * Endpoint to retrieve details of a user based on the provided username.
     *
     * @param username The username of the user to retrieve.
     * @return ResponseEntity containing the User object or an unauthorized message if the user is not authorized.
     * @throws RuntimeException If there's an issue retrieving the user.
     */
    @GetMapping("/getUserByUsername")
    public ResponseEntity<User> getUserByUsername(@RequestParam String username) {
        try {
            // Retrieve the user using the userService and return it in the ResponseEntity
            return ResponseEntity.ok().body(userService.getUserByUserName(username));
        } catch (Exception e) {
            // Log the error and return an unauthorized response
            log.error("Error retrieving user by username: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
