package com.example.ecommerce.api_gateway.filters;

import com.example.ecommerce.api_gateway.service.JwtService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final JwtService jwtService;

    public AuthenticationGatewayFilterFactory(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            if (!config.enabled) {
                ServerWebExchange bypassExchange = exchange.mutate()
                        .request(request -> request.header("X-User-Id", "1"))
                        .build();

                return chain.filter(bypassExchange);
            }

String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authorizationHeader.substring(7);

            Long userId = jwtService.getUserIdFromToken(token);
            ServerWebExchange authenticatedExchange = exchange.mutate()
                    .request(request -> request.header("X-User-Id", userId.toString()))
                    .build();

            return chain.filter(authenticatedExchange);
        };
    }

    @Getter
    @Setter
    public static class Config {
        private Boolean enabled = true;
    }
}
