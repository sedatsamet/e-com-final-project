package org.sedatsamet.orderservice.util;

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
    // TODO: complete the url
    private String updateProductQuantityUrl = "http://product-service/product/updateProductQuantity";
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

    public Cart getCartByUserId(UUID userId) {
        Cart cart;
        try {
            cart = restTemplate.exchange(cartServiceUrl + userId.toString(), HttpMethod.GET, httpEntity, Cart.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Cart not found");
        }
        return cart != null ? cart : null;
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

    public CartItem getCartItemFromProductService(UUID productId) {
        CartItem cartItem;
        try {
            cartItem = restTemplate.exchange(productServiceUrl + productId.toString(), HttpMethod.GET, httpEntity, CartItem.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Product not found");
        }
        return cartItem != null ? cartItem : null;
    }

    public void updateUserOrderIdList(User user) {
        HttpEntity<User> httpEntity = new HttpEntity<>(user, headers);
        User updatedUser = restTemplate.exchange(userServiceUpdateUser, HttpMethod.PUT, httpEntity, User.class, user).getBody();
    }

    public void setUpdateProductQuantity(UUID productId, Integer quantity) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("productId", String.valueOf(productId));
            params.add("quantity", quantity.toString());
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
            CartItem cartItem = restTemplate.exchange(updateProductQuantityUrl, HttpMethod.PUT, httpEntity, CartItem.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Product not found while updating quantity with productId: " + productId);
        }
    }
}
