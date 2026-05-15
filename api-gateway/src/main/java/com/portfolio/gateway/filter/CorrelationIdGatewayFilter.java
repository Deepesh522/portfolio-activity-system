package com.portfolio.gateway.filter;

import com.portfolio.shared.constant.CorrelationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

//Global gateway filter that generates or propagates a Correlation ID across all downstream service calls.
@Component
@Slf4j
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();

                String correlationId = request.getHeaders()
                                .getFirst(CorrelationConstants.CORRELATION_ID_HEADER);

                if (correlationId == null || correlationId.isBlank()) {
                        correlationId = UUID.randomUUID().toString();
                }

                final String finalCorrelationId = correlationId;

                log.info("Gateway request: method={} path={} correlationId={}",
                                request.getMethod(), request.getURI().getPath(), finalCorrelationId);

                ServerHttpRequest mutatedRequest = request.mutate()
                                .header(CorrelationConstants.CORRELATION_ID_HEADER, finalCorrelationId)
                                .build();

                exchange.getResponse().getHeaders()
                                .add(CorrelationConstants.CORRELATION_ID_HEADER, finalCorrelationId);

                return chain.filter(exchange.mutate().request(mutatedRequest).build())
                                .then(Mono.fromRunnable(
                                                () -> log.info("Gateway response: path={} status={} correlationId={}",
                                                                request.getURI().getPath(),
                                                                exchange.getResponse().getStatusCode(),
                                                                finalCorrelationId)));
        }

        @Override
        public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE;
        }
}
