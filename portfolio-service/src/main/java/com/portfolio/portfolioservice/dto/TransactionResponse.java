package com.portfolio.portfolioservice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record TransactionResponse(
                UUID id,
                String userId,
                String symbol,
                String type,
                int quantity,
                BigDecimal price,
                BigDecimal totalValue,
                Instant createdAt) {
}
