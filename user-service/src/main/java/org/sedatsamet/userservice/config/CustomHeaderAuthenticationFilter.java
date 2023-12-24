package org.sedatsamet.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.userservice.entity.User;
import org.sedatsamet.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    private UserService userService;
    private static final String LOGGED_IN_USER_HEADER = "loggedInUser";

    /**
     * Filters and processes the incoming HTTP request to set authentication details.
     * If a valid loggedInUsername is found in the request headers, it sets the authentication context.
     *
     * @param request     The HTTP request.
     * @param response    The HTTP response.
     * @param filterChain The filter chain for the request.
     * @throws ServletException If there's an issue during the filter processing.
     * @throws IOException      If there's an I/O error during the filter processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Retrieve the loggedInUsername from the request headers
        String loggedInUsername = request.getHeader(LOGGED_IN_USER_HEADER);
        // Fetch the logged-in user details based on the retrieved username
        User loggedInUser = getLoggedInUser(loggedInUsername);
        // If a valid username is present, set the authentication context
        if (StringUtils.hasText(loggedInUsername)) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(loggedInUser, null, loggedInUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Retrieves the logged-in user details based on the provided username.
     *
     * @param username The username of the logged-in user.
     * @return The User object representing the logged-in user.
     * @throws RuntimeException If there's an issue retrieving the user details.
     */
    private User getLoggedInUser(String username) {
        try {
            // Fetch the logged-in user details using the userService
            User loggedInUser = userService.getUserByUserName(username);
            return loggedInUser;
        } catch (Exception e) {
            // Log the error and throw a runtime exception with a generic message
            log.error("Error fetching user details for username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error retrieving user details");
        }
    }
}
