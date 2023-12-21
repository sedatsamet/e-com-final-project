package org.sedatsamet.orderservice.util;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.orderservice.entity.Cart;
import org.sedatsamet.orderservice.entity.CartItem;
import org.sedatsamet.orderservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class OrderUtil {
    @Autowired
    private RestTemplate restTemplate;
    private HttpHeaders headers = new HttpHeaders();
    private HttpEntity<String> httpEntity;
    private String userServiceUrlById = "http://user-service/user/getUser?userId=";
    private String userServiceUrlByUsername = "http://user-service/user/getUserByUsername?username=";
    private String userServiceUpdateUser = "http://user-service/user/updateUser";
    private String productServiceUrl = "http://product-service/product/getProductById?productId=";
    private String cartServiceUrl = "http://cart-service/cart/getCartByUserIdForOrderService?userId=";
    private String updateProductQuantityUrl = "http://product-service/product/updateProductQuantity";
    private String username;

    /**
     * Retrieves the details of a logged-in user by username.
     *
     * @param username The username of the logged-in user.
     * @return The details of the logged-in user.
     * @throws UsernameNotFoundException if the user is not found or other unexpected issues occur.
     */
    public User getLoggedInUserDetailsByUsername(String username) {
        this.username = username;
        setHeaders(this.username);
        User loggedInUser;
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlByUsername + this.username, HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found. Reason: " + e.getMessage());
        }
        return loggedInUser;
    }

    /**
     * Retrieves the details of a user by user ID.
     *
     * @param userId The ID of the user.
     * @return The details of the user.
     * @throws RuntimeException if the user is not found or other unexpected issues occur.
     */
    public User getUserDetailsByUserId(UUID userId) {
        User loggedInUser;
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlById + userId.toString(), HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("User not found. Reason: " + e.getMessage());
        }
        return loggedInUser;
    }

    /**
     * Retrieves the cart associated with a user by user ID.
     *
     * @param userId The ID of the user.
     * @return The cart associated with the user or null if not found.
     * @throws RuntimeException if the cart is not found or other unexpected issues occur.
     */
    public Cart getCartByUserId(UUID userId) {
        Cart cart;
        try {
            cart = restTemplate.exchange(cartServiceUrl + userId.toString(), HttpMethod.GET, httpEntity, Cart.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Cart not found. Reason: " + e.getMessage());
        }
        return cart;
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
     * Checks if the currently authenticated user is an admin.
     *
     * @return true if the authenticated user is an admin, false otherwise.
     * @throws RuntimeException If there's an issue determining the user's role.
     */
    public Boolean isUserAdmin() {
        try {
            // Retrieve the authentication details for the current context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Extract the principal user from the authentication details
            User authenticatedUser = (User) authentication.getPrincipal();

            // Check if the user's authorities contain the "ROLE_ADMIN" authority
            if (authenticatedUser.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            // In case of any exceptions, log the error and throw a runtime exception
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

    /**
     * Retrieves a cart item from the product service based on the provided product ID.
     *
     * @param productId The UUID of the product for which the cart item is to be retrieved.
     * @return A CartItem object representing the details of the product.
     * @throws RuntimeException If the product is not found or if any other error occurs during the retrieval.
     */
    public CartItem getCartItemFromProductService(UUID productId) {
        CartItem cartItem;

        try {
            // Attempt to retrieve the cart item using the provided product ID
            cartItem = restTemplate.exchange(productServiceUrl + productId.toString(), HttpMethod.GET, httpEntity, CartItem.class).getBody();
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error retrieving cart item for product with ID {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Product not found");
        }

        return cartItem;
    }

    /**
     * Updates the order ID list of a given user.
     *
     * @param user The User object containing the updated order ID list.
     * @throws RuntimeException If there's an error during the update operation.
     */
    public void updateUserOrderIdList(User user) {
        try {
            HttpEntity<User> httpEntity = new HttpEntity<>(user, headers);
            User updatedUser = restTemplate.exchange(userServiceUpdateUser, HttpMethod.PUT, httpEntity, User.class, user).getBody();
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error updating order ID list for user with ID {}: {}", user.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Error updating user order ID list");
        }
    }

    /**
     * Updates the quantity of a product based on the given product ID.
     *
     * @param productId The ID of the product whose quantity needs to be updated.
     * @param quantity The new quantity value to set for the product.
     * @throws RuntimeException If the product is not found or if there's an error during the update operation.
     */
    public void setUpdateProductQuantity(UUID productId, Integer quantity) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("productId", String.valueOf(productId));
            params.add("quantity", quantity.toString());
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
            CartItem cartItem = restTemplate.exchange(updateProductQuantityUrl, HttpMethod.PUT, httpEntity, CartItem.class).getBody();
        } catch (Exception e) {
            // Log the error and throw a runtime exception
            log.error("Error updating quantity for product with ID {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Product not found while updating quantity with productId: " + productId);
        }
    }
}
