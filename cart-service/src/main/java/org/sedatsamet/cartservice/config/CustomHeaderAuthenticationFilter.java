package org.sedatsamet.cartservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.cartservice.entity.User;
import org.sedatsamet.cartservice.util.CartUtil;
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
    private CartUtil cartUtil;
    private static final String LOGGED_IN_USER_HEADER = "loggedInUser";


    /**
     * Filters the incoming HTTP request to set the authentication context based on the logged-in user.
     *
     * @param request       The HTTP request.
     * @param response      The HTTP response.
     * @param filterChain   The filter chain.
     * @throws ServletException If there's an issue processing the servlet request.
     * @throws IOException      If there's an I/O error while processing the request.
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
            // Log the exception for debugging purposes
            log.error("Error processing authentication filter: {}", e.getMessage(), e);
            throw new ServletException("Failed to process authentication filter", e);
        }
    }

    /**
     * Retrieves the logged-in user details based on the provided username.
     *
     * @param username The username of the logged-in user.
     * @return The User object representing the logged-in user.
     * @throws UsernameNotFoundException If the user is not found.
     */
    private User getLoggedInUser(String username) {
        try {
            return cartUtil.getLoggedInUserDetailsByUsername(username);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("Error fetching logged-in user details: {}", e.getMessage(), e);
            throw new UsernameNotFoundException("User not found", e);
        }
    }
}
