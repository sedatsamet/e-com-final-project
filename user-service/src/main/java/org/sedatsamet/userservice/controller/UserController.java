package org.sedatsamet.userservice.controller;

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
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/allUsers")
    public List<User> listAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/register")
    public ResponseEntity<User> createNewUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok().body(userService.createUser(request));
    }

    @PutMapping("/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequest request) {
        try {
            return ResponseEntity.ok().body(userService.updateUser(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to update this user");
        }
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(@RequestParam String userId) {
        try {
            return ResponseEntity.ok().body(userService.getUser(UUID.fromString(userId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to view this user");
        }
    }

    @GetMapping("/getUserByUsername")
    public ResponseEntity<User> getUserByUsername(@RequestParam String username) {
        return ResponseEntity.ok().body(userService.getUserByUserName(username));
    }
}
