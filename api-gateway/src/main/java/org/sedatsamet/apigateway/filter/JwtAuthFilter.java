package org.sedatsamet.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
    @Autowired
    private RouteValidator routeValidator;
    @Autowired
    private WebClient.Builder webClientBuilder;
    private final String authValidateUrl = "http://auth-service/auth/validate?token=";

    public JwtAuthFilter() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if(routeValidator.isSecured.test(exchange.getRequest())) {
                // header contains header or not
                if(!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                    throw new RuntimeException("Authorization header is missing");
                }
                String authHeader = exchange.getRequest().getHeaders().get("Authorization").get(0);
                if(authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    String response = webClientBuilder.build()
                            .get()
                            .uri(authValidateUrl + authHeader).retrieve().bodyToMono(String.class).block();
                    if(!response.equals("Token is valid")) {
                        throw new RuntimeException("Invalid access");
                    }
                    //jwtUtil.validateToken(authHeader);
                }catch (Exception e) {
                    throw new RuntimeException("Authorization token is not valid");
                }
            }
            return chain.filter(exchange);
        });
    }
    public static class Config{}
}
