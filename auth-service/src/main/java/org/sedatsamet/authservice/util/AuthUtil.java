package org.sedatsamet.authservice.util;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.authservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AuthUtil {
    @Autowired
    private RestTemplate restTemplate;
    private String userServiceUrlByUsername = "http://user-service/user/getUserByUsername?username=";
    private HttpHeaders headers = new HttpHeaders();
    private HttpEntity<String> httpEntity;
    private String username;
    /**
     * Retrieves user details for the provided username from a user service.
     *
     * @param username The username for which user details are to be retrieved.
     * @return The user details for the provided username.
     * @throws UsernameNotFoundException If the user with the provided username is not found.
     */
    public User getLoggedInUserDetailsByUsername(String username) {
        this.username = username;
        setHeaders(this.username);
        User loggedInUser;
        try {
            loggedInUser = restTemplate.exchange(userServiceUrlByUsername + this.username, HttpMethod.GET, httpEntity, User.class).getBody();
        } catch (Exception e) {
            // Log the exception for debugging purposes and throw a custom exception
            log.error("Error retrieving user details for username: {}", username, e);
            throw new UsernameNotFoundException("User not found for username: " + username);
        }
        return loggedInUser;
    }

    /**
     * Sets the necessary headers for making HTTP requests.
     *
     * @param username The username to set in the headers.
     */
    private void setHeaders(String username) {
        try {
            headers.set("loggedInUser", username);
            httpEntity = new HttpEntity<>(headers);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error setting headers for username: {}", username, e);
            throw new RuntimeException("Failed to set headers", e);
        }
    }
}
