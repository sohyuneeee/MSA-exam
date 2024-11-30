package com.sparta.msa_exam.gateway.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CustomPreFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CustomPreFilter.class.getName());


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Long startTime = System.currentTimeMillis();
        exchange.getAttributes().put("startTime", startTime);

        String traceId = request.getHeaders().getFirst("X-Request-Id");
        if(traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        exchange.getAttributes().put("traceId", traceId);

        logger.info("[{}] Incoming Request: Method = {}, path = {}", traceId, request.getMethod(), request.getURI());

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
