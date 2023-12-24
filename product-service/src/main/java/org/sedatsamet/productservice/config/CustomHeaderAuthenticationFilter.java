package org.sedatsamet.productservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.productservice.entity.User;
import org.sedatsamet.productservice.util.ProductUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class CustomHeaderAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private ProductUtil productUtil;
    private static final String LOGGED_IN_USER_HEADER = "loggedInUser";

    /**
     * This method is responsible for intercepting HTTP requests to check for a logged-in user.
     * If a valid user is detected, the method sets the user's authentication details in the security context.
     *
     * @param request The incoming HTTP request.
     * @param response The HTTP response.
     * @param filterChain The chain of filters to be applied.
     * @throws ServletException If there's a servlet-related exception.
     * @throws IOException If there's an I/O related exception.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Extract the username of the logged-in user from the request header
        String loggedInUsername = request.getHeader(LOGGED_IN_USER_HEADER);
        User loggedInUser;
        try {
            // Attempt to fetch the details of the logged-in user
            loggedInUser = getLoggedInUser(loggedInUsername);
        } catch (Exception e) {
            // In case of any exceptions, log the error and continue with the filter chain
            log.error("Error retrieving logged-in user {}: {}", loggedInUsername, e.getMessage(), e);
            filterChain.doFilter(request, response);
            return;
        }
        // If a valid username is present, create an authentication token and set it in the security context
        if (StringUtils.hasText(loggedInUsername)) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(loggedInUser, null, loggedInUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // Continue with the filter chain processing
        filterChain.doFilter(request, response);
    }

    /**
     * Retrieves details of the logged-in user based on the provided username.
     *
     * @param username The username of the logged-in user.
     * @return The User object containing details of the logged-in user.
     * @throws RuntimeException If there's an issue retrieving the user details.
     */
    private User getLoggedInUser(String username) {
        try {
            // Attempt to fetch the details of the logged-in user using the provided username
            return productUtil.getLoggedInUserDetailsByUsername(username);
        } catch (Exception e) {
            // In case of any exceptions, log the error and throw a runtime exception
            log.error("Error retrieving details for logged-in user with username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error retrieving logged-in user details");
        }
    }
}
