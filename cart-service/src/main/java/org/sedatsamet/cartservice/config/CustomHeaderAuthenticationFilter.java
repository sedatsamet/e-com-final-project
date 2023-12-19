package org.sedatsamet.cartservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sedatsamet.cartservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CustomHeaderAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private RestTemplate restTemplate;
    private static final String LOGGED_IN_USER_HEADER = "loggedInUser";

    private String userServiceUrl = "http://user-service/user/getUserByUsername?username=";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String loggedInUsername = request.getHeader(LOGGED_IN_USER_HEADER);
        User loggedInUser = getLoggedInUser(loggedInUsername);

        if(StringUtils.hasText(loggedInUsername)) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    loggedInUser,
                    null,
                    loggedInUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private User getLoggedInUser(String username) {
        User loggedInUser = restTemplate.getForEntity(userServiceUrl + username, User.class).getBody();
        System.out.println("Deneme loggedInUser " + loggedInUser);
        return loggedInUser;
    }

}
