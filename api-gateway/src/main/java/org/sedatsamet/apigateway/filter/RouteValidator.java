package org.sedatsamet.apigateway.filter;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    /**
     * List of endpoints that are open and do not require authentication.
     * These endpoints are accessible without needing a valid JWT token.
     */
    public static final List<String> openApiEndpoints = List.of(
            "/user/register",
            "/auth/login",
            "/eureka",
            "/v3/api-docs");
    /**
     * Predicate to determine if a given request is for an open (unsecured) endpoint.
     * Checks if the request URI path matches any of the endpoints listed in {@code openApiEndpoints}.
     *
     * @param request The server HTTP request to check.
     * @return {@code true} if the request is for an open endpoint, {@code false} otherwise.
     */
    public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
            .stream()
            .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
