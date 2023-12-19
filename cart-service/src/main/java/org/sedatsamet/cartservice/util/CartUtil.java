package org.sedatsamet.cartservice.util;

import org.sedatsamet.cartservice.dto.CartItemRequest;
import org.sedatsamet.cartservice.entity.CartItem;
import org.sedatsamet.cartservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
        User loggedInUser = restTemplate.exchange(userServiceUrlByUsername + this.username, HttpMethod.GET, httpEntity, User.class).getBody();
        return loggedInUser;
    }

    public User getUserDetailsByUserId(UUID userId) {
        User loggedInUser = restTemplate.exchange(userServiceUrlById + userId, HttpMethod.GET, httpEntity, User.class).getBody();
        return loggedInUser;
    }

    public CartItem getCartItemFromProductService(CartItemRequest request) {
        CartItem cartItem = restTemplate.exchange(productServiceUrl + request.getProductId(), HttpMethod.GET, httpEntity, CartItem.class).getBody();
        return cartItem != null ? cartItem : null;
    }

    private void setHeaders(String username) {
        headers.set("loggedInUser", username);
        httpEntity = new HttpEntity<>(headers);
    }
}
