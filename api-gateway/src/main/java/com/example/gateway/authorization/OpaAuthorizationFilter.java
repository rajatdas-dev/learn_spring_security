package com.example.gateway.authorization;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class OpaAuthorizationFilter implements GlobalFilter, Ordered {

    private final WebClient opaClient;

    public OpaAuthorizationFilter(
            WebClient.Builder builder,
            @Value("${security.opa.url}") String opaUrl) {

        this.opaClient = builder
                .baseUrl(opaUrl)
                .build();
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        // Public endpoints don't need an OPA decision.
        if (path.equals("/auth/login")
                || path.equals("/auth/register")
                || path.equals("/.well-known/jwks.json")) {

            return chain.filter(exchange);
        }

        return exchange.getPrincipal()

                .cast(Authentication.class)

                .flatMap(authentication -> {

                    if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
                        return Mono.just(false);
                    }

                    var jwt = jwtAuth.getToken();

                    String role = jwt.getClaimAsString("role");

                    Map<String, Object> input = Map.of(

                            "subject", Map.of(
                                    "id", jwt.getClaimAsString("uid"),
                                    "username", jwt.getSubject(),
                                    "role", role == null ? "" : role
                            ),

                            "request", Map.of(
                                    "method", exchange.getRequest()
                                            .getMethod()
                                            .name(),

                                    "path", path
                            )
                    );

                    return opaClient.post()

                            .uri("/v1/data/spring_security/allow")

                            .bodyValue(Map.of("input", input))

                            .retrieve()

                            .bodyToMono(Decision.class)

                            .map(Decision::result);
                })

                .defaultIfEmpty(false)

                // Do not allow requests if OPA is unavailable.
                .onErrorReturn(false)

                .flatMap(allowed -> {

                    if (allowed) {
                        return chain.filter(exchange);
                    }

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);

                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        return -10;
    }

    public record Decision(boolean result) {
    }
}