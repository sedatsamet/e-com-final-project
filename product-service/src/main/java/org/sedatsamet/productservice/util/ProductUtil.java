package org.sedatsamet.productservice.util;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.productservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ProductUtil {
    @Autowired
    private RestTemplate restTemplate;
    private HttpHeaders headers = new HttpHeaders();
    private HttpEntity<String> httpEntity;
    private String userServiceUrlByUsername = "http://user-service/user/getUserByUsername?username=";
    private String username;

    /**
     * Retrieves details of the logged-in user using the provided username.
     *
     * @param username The username of the logged-in user.
     * @return The details of the logged-in user.
     * @throws RuntimeException If the user details cannot be retrieved or if the user is not found.
     */
    public User getLoggedInUserDetailsByUsername(String username) {
        // Set the username and headers for the HTTP request
        this.username = username;
        setHeaders(this.username);

        User loggedInUser;

        // Attempt to retrieve the user details from the user service
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlByUsername + this.username, HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            // Log the error and throw a runtime exception if the user details cannot be retrieved
            log.error("Error retrieving user details for username {}: {}", this.username, e.getMessage(), e);
            throw new RuntimeException("User not found");
        }

        // Return the retrieved user details
        return loggedInUser;
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
}
