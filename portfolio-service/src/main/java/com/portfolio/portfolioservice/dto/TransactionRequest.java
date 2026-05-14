package com.portfolio.portfolioservice.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionRequest(

        @NotBlank(message = "userId is required") String userId,

        @NotBlank(message = "symbol is required") @Size(max = 10, message = "symbol must be at most 10 characters") String symbol,

        @NotNull(message = "type is required (BUY or SELL)") TransactionTypeDTO type,

        @Positive(message = "quantity must be positive") int quantity,

        @NotNull(message = "price is required") @DecimalMin(value = "0.01", message = "price must be greater than 0") BigDecimal price) {
    public enum TransactionTypeDTO {
        BUY, SELL
    }
}
