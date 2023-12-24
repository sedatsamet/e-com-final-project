package org.sedatsamet.cartservice.util;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class CartUtil {
    @Autowired
    private RestTemplate restTemplate;
    private HttpHeaders headers = new HttpHeaders();
    private HttpEntity<String> httpEntity;
    private String userServiceUrlById = "http://user-service/user/getUser?userId=";
    private String userServiceUrlByUsername = "http://user-service/user/getUserByUsername?username=";
    private String productServiceUrl = "http://product-service/product/getProductById?productId=";
    private String username;

    /**
     * Retrieves the details of the logged-in user by username.
     *
     * @param username The username of the logged-in user.
     * @return A User object representing the details of the logged-in user.
     * @throws UsernameNotFoundException If the user is not found, a UsernameNotFoundException is thrown.
     * @throws RuntimeException If any other error occurs during the retrieval, a runtime exception is thrown.
     */
    public User getLoggedInUserDetailsByUsername(String username) {
        this.username = username;
        setHeaders(this.username);
        User loggedInUser = null;
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlByUsername + this.username, HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            // Log the error and throw appropriate exceptions
            log.error("Error retrieving details for logged-in user: {}", e.getMessage(), e);

            // Check if the error is due to user not found
            if (e instanceof HttpClientErrorException.NotFound) {
                throw new UsernameNotFoundException("User not found");
            } else {
                throw new RuntimeException("Error retrieving details for logged-in user");
            }
        }
        return loggedInUser;
    }

    /**
     * Retrieves the details of a user by user ID.
     *
     * @param userId The ID of the user.
     * @return A User object representing the details of the user.
     * @throws RuntimeException If the user is not found or if any other error occurs during the retrieval.
     */
    public User getUserDetailsByUserId(UUID userId) {
        User loggedInUser = null;
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlById + userId.toString(), HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error retrieving details for user with ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("User not found");
        }
        return loggedInUser;
    }

    /**
     * Retrieves a cart item from the product service based on the provided product ID.
     *
     * @param request The CartItemRequest containing the product ID.
     * @return A CartItem object representing the details of the product.
     * @throws RuntimeException If the product is not found or if any other error occurs during the retrieval.
     */
    public CartItem getCartItemFromProductService(CartItemRequest request) {
        CartItem cartItem = null;
        try {
            cartItem = restTemplate.exchange(productServiceUrl + request.getProductId().toString(), HttpMethod.GET, httpEntity, CartItem.class).getBody();
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error retrieving cart item for product with ID {}: {}", request.getProductId(), e.getMessage(), e);
            throw new RuntimeException("Product not found");
        }
        return cartItem;
    }

    /**
     * Sets headers for the HTTP request, specifically setting the "loggedInUser" header.
     *
     * @param username The username to be set in the header.
     * @throws RuntimeException If there's an issue setting the headers.
     */
    private void setHeaders(String username) {
        try {
            headers.set("loggedInUser", username);
            httpEntity = new HttpEntity<>(headers);
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error setting headers for username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error setting headers");
        }
    }

    /**
     * Checks if the authenticated user is an admin.
     *
     * @return true if the user is an admin, false otherwise.
     * @throws RuntimeException If there's an issue determining the user's role.
     */
    public Boolean isUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User authenticatedUser = (User) authentication.getPrincipal();
            if (authenticatedUser.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error determining if user is admin: {}", e.getMessage(), e);
            throw new RuntimeException("Error determining user role");
        }
    }

    /**
     * Retrieves the authenticated user from the security context.
     *
     * @return The authenticated user.
     * @throws RuntimeException If there's an issue retrieving the authenticated user.
     */
    public User getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User authenticatedUser = (User) authentication.getPrincipal();
            return authenticatedUser;
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error retrieving authenticated user: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving authenticated user");
        }
    }
}
