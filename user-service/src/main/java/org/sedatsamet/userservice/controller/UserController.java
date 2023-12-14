package org.sedatsamet.userservice.controller;

import org.sedatsamet.userservice.dto.request.UserCreateRequest;
import org.sedatsamet.userservice.entity.User;
import org.sedatsamet.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> createNewUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok().body(userService.createUser(request));
    }

}
