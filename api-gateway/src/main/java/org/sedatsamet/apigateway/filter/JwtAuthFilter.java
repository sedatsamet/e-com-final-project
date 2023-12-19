package org.sedatsamet.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.sedatsamet.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
    @Autowired
    private RouteValidator routeValidator;
    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = null;
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                // header contains header or not
                if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                    throw new RuntimeException("Authorization header is missing");
                }
                String authHeader = exchange.getRequest().getHeaders().get("Authorization").get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtUtil.validateToken(authHeader);
                    request = exchange.getRequest().mutate().header("loggedInUser", jwtUtil.extractUserNameFromToken(authHeader)).build();
                } catch (Exception e) {
                    throw new RuntimeException("Authorization token is not valid");
                }
            }
            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    public static class Config {
    }
}
