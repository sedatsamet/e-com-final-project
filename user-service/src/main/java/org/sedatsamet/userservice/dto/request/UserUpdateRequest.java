package org.sedatsamet.userservice.dto.request;

import lombok.Data;
import org.sedatsamet.userservice.entity.Role;

import java.util.Set;
import java.util.UUID;

@Data
public class UserUpdateRequest {
    private UUID userId;
    private String name;
    private String surName;
    private String telephone;
    private String username;
    private String password;
    private Set<Role> authorities;
}
