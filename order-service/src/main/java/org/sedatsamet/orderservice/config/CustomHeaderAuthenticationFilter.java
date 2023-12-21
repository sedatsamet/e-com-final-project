package org.sedatsamet.orderservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.orderservice.entity.User;
import org.sedatsamet.orderservice.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class CustomHeaderAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private OrderUtil orderUtil;
    private static final String LOGGED_IN_USER_HEADER = "loggedInUser";


    /**
     * Custom filter to set the authentication in the security context based on the logged-in username.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException If there's an issue with the servlet processing.
     * @throws IOException If there's an issue with I/O operations.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String loggedInUsername = request.getHeader(LOGGED_IN_USER_HEADER);
            User loggedInUser = getLoggedInUser(loggedInUsername);
            if (StringUtils.hasText(loggedInUsername)) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(loggedInUser, null, loggedInUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Log the error and re-throw it
            log.error("Error in doFilterInternal: {}", e.getMessage(), e);
            throw new ServletException("Error processing authentication filter", e);
        }
    }

    /**
     * Retrieves the logged-in user details based on the given username.
     *
     * @param username The username of the logged-in user.
     * @return The User object representing the logged-in user.
     * @throws UsernameNotFoundException If the user is not found.
     */
    private User getLoggedInUser(String username) {
        try {
            return orderUtil.getLoggedInUserDetailsByUsername(username);
        } catch (Exception e) {
            // Log the error and re-throw it
            log.error("Error in getLoggedInUser: {}", e.getMessage(), e);
            throw new UsernameNotFoundException("User not found", e);
        }
    }
}
