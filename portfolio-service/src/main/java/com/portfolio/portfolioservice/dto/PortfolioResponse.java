package com.portfolio.portfolioservice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record PortfolioResponse(
                String userId,
                List<HoldingDTO> holdings,
                BigDecimal totalPortfolioValue) {
        @Builder
        public record HoldingDTO(
                        String symbol,
                        int quantity,
                        BigDecimal averagePrice,
                        BigDecimal totalInvested) {
        }
}
