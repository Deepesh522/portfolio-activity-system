package com.portfolio.portfolioservice.controller;

import com.portfolio.portfolioservice.dto.PortfolioResponse;
import com.portfolio.portfolioservice.dto.TransactionRequest;
import com.portfolio.portfolioservice.dto.TransactionResponse;
import com.portfolio.portfolioservice.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for portfolio transaction operations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio transaction management APIs")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("/transactions")
    @Operation(summary = "Create a new transaction", description = "Submit a BUY or SELL transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse response = portfolioService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/portfolio/{userId}")
    @Operation(summary = "Get portfolio summary", description = "Get holdings summary for a user")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable String userId) {

        PortfolioResponse response = portfolioService.getPortfolio(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{userId}")
    @Operation(summary = "Get transaction history", description = "Get paginated transaction history for a user")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<TransactionResponse> response = portfolioService.getTransactions(userId, pageable);
        return ResponseEntity.ok(response);
    }
}
