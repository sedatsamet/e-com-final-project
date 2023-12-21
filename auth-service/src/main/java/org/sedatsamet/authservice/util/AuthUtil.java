package org.sedatsamet.authservice.util;

import org.sedatsamet.authservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthUtil {
    @Autowired
    private RestTemplate restTemplate;
    private String userServiceUrlByUsername = "http://user-service/user/getUserByUsername?username=";
    private HttpHeaders headers = new HttpHeaders();
    private HttpEntity<String> httpEntity;
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
    private void setHeaders(String username) {
        headers.set("loggedInUser", username);
        httpEntity = new HttpEntity<>(headers);
    }
}
