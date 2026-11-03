package com.example.gateway.ratelimit;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class InMemoryRateLimitFilter implements GlobalFilter, Ordered {

    private final ConcurrentHashMap<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    private final double capacity;
    private final double refillPerSecond;

    public InMemoryRateLimitFilter(
            @Value("${security.rate-limit.capacity:60}") int capacity,
            @Value("${security.rate-limit.refill-per-second:1}") double refillPerSecond) {

        if (capacity < 1 || refillPerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Invalid rate limit configuration"
            );
        }

        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        return exchange.getPrincipal()

                .map(Principal::getName)

                // For unauthenticated requests, use remote IP.
                .defaultIfEmpty(getClientIp(exchange))

                .flatMap(clientKey -> {

                    Bucket bucket = buckets.computeIfAbsent(
                            clientKey,
                            key -> new Bucket(capacity)
                    );

                    boolean allowed = bucket.tryConsume(
                            capacity,
                            refillPerSecond
                    );

                    exchange.getResponse()
                            .getHeaders()
                            .set("X-RateLimit-Limit",
                                    String.valueOf((int) capacity));

                    if (!allowed) {

                        exchange.getResponse()
                                .setStatusCode(
                                        HttpStatus.TOO_MANY_REQUESTS
                                );

                        exchange.getResponse()
                                .getHeaders()
                                .set("Retry-After", "1");

                        return exchange.getResponse().setComplete();
                    }

                    return chain.filter(exchange);
                });
    }

    private String getClientIp(ServerWebExchange exchange) {

        if (exchange.getRequest().getRemoteAddress() == null) {
            return "unknown-client";
        }

        return exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
    }

    @Override
    public int getOrder() {
        return -5;
    }

    private static class Bucket {

        private double tokens;
        private long lastRefillTime;

        Bucket(double capacity) {
            this.tokens = capacity;
            this.lastRefillTime = System.nanoTime();
        }

        synchronized boolean tryConsume(
                double capacity,
                double refillPerSecond) {

            long now = System.nanoTime();

            double elapsedSeconds =
                    (now - lastRefillTime) / 1_000_000_000.0;

            tokens = Math.min(
                    capacity,
                    tokens + elapsedSeconds * refillPerSecond
            );

            lastRefillTime = now;

            if (tokens < 1) {
                return false;
            }

            tokens -= 1;
            return true;
        }
    }
}