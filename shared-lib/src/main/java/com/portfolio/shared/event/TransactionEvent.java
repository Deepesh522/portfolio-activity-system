package com.portfolio.shared.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable event published by Portfolio Service after a successful transaction commit.
 * Consumed by Notification Service via RabbitMQ.
 */
@Builder
public record TransactionEvent(
        String eventId,
        String correlationId,
        String userId,
        String symbol,
        TransactionType type,
        int quantity,
        BigDecimal price,
        BigDecimal totalValue,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public enum TransactionType {
        BUY, SELL
    }
}
