package org.sedatsamet.cartservice.util;

import org.sedatsamet.cartservice.dto.CartItemRequest;
import org.sedatsamet.cartservice.entity.CartItem;
import org.sedatsamet.cartservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class CartUtil {
    @Autowired
    private RestTemplate restTemplate;
    private HttpHeaders headers = new HttpHeaders();
    private HttpEntity<String> httpEntity;
    private String userServiceUrlById = "http://user-service/user/getUser?userId=";
    private String userServiceUrlByUsername = "http://user-service/user/getUserByUsername?username=";
    private String productServiceUrl = "http://product-service/product/getProductById?productId=";
    private String username;

    public User getLoggedInUserDetailsByUsername(String username) {
        this.username = username;
        setHeaders(this.username);
        User loggedInUser;
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlByUsername + this.username, HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }
        return loggedInUser;
    }

    public User getUserDetailsByUserId(UUID userId) {
        User loggedInUser;
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlById + userId.toString(), HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("User not found");
        }
        return loggedInUser;
    }

    public CartItem getCartItemFromProductService(CartItemRequest request) {
        CartItem cartItem;
        try {
            cartItem = restTemplate.exchange(productServiceUrl + request.getProductId().toString(), HttpMethod.GET, httpEntity, CartItem.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Product not found");
        }
        return cartItem != null ? cartItem : null;
    }

    private void setHeaders(String username) {
        headers.set("loggedInUser", username);
        httpEntity = new HttpEntity<>(headers);
    }

    public Boolean isUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User authenticatedUser = (User) authentication.getPrincipal();
            if (authenticatedUser.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public User getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User authenticatedUser = (User) authentication.getPrincipal();
            return authenticatedUser;
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
