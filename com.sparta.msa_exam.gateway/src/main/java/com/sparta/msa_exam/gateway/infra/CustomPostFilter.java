package com.sparta.msa_exam.gateway.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomPostFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CustomPostFilter.class.getName());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute("startTime");
            String traceId = exchange.getAttribute("traceId");

            if (startTime != null && traceId != null) {
                Long duration = System.currentTimeMillis() - startTime;

                ServerHttpResponse response = exchange.getResponse();
                Integer statusCode = response.getStatusCode() != null ? response.getStatusCode().value() :  - 1;
                logger.info("[{}] Outgoing Response: StatusCode = {}, Duration = {}ms", traceId, statusCode, duration);

                String serverPort = response.getHeaders().getFirst("Server-Port");
                logger.info("[{}] Server-Port = {}", traceId, serverPort);
            }

        }));
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}