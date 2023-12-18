package org.sedatsamet.userservice.controller;

import org.sedatsamet.userservice.dto.request.UserCreateRequest;
import org.sedatsamet.userservice.dto.request.UserUpdateRequest;
import org.sedatsamet.userservice.entity.User;
import org.sedatsamet.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<User> updateUser(@RequestBody UserUpdateRequest request) {
        User updatedUser = userService.updateUser(request);
        return updatedUser != null ? ResponseEntity.ok().body(updatedUser) : ResponseEntity.notFound().build();
    }

    @GetMapping("/getUser")
    public ResponseEntity<User> getUser(@RequestParam String userId) {
        return ResponseEntity.ok().body(userService.getUser(UUID.fromString(userId)));
    }

}
