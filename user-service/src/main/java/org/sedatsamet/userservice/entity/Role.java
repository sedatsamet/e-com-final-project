package org.sedatsamet.userservice.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_CUSTOMER, ROLE_ADMIN;
    @Override
    public String getAuthority() {
        return name();
    }
}
