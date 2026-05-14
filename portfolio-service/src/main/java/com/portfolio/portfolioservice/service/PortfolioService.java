package com.portfolio.portfolioservice.service;

import com.portfolio.portfolioservice.dto.PortfolioResponse;
import com.portfolio.portfolioservice.dto.TransactionRequest;
import com.portfolio.portfolioservice.dto.TransactionResponse;
import com.portfolio.portfolioservice.entity.Holding;
import com.portfolio.portfolioservice.entity.Transaction;
import com.portfolio.portfolioservice.exception.InsufficientHoldingsException;
import com.portfolio.portfolioservice.messaging.TransactionEventPublisher;
import com.portfolio.portfolioservice.repository.HoldingRepository;
import com.portfolio.portfolioservice.repository.TransactionRepository;
import com.portfolio.shared.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.portfolio.shared.constant.CorrelationConstants.CORRELATION_ID_MDC_KEY;

/**
 * Core business logic for portfolio transactions and holdings management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

        private final TransactionRepository transactionRepository;
        private final HoldingRepository holdingRepository;
        private final TransactionEventPublisher eventPublisher;

        /**
         * Process a new transaction: persist it, update holdings, and publish an event.
         */
        @Transactional
        public TransactionResponse createTransaction(TransactionRequest request) {
                log.info("Processing {} transaction for user={} symbol={} qty={} price={}",
                                request.type(), request.userId(), request.symbol(), request.quantity(),
                                request.price());

                BigDecimal totalValue = request.price().multiply(BigDecimal.valueOf(request.quantity()));

                // Persist the transaction
                Transaction transaction = Transaction.builder()
                                .userId(request.userId())
                                .symbol(request.symbol().toUpperCase())
                                .type(Transaction.TransactionType.valueOf(request.type().name()))
                                .quantity(request.quantity())
                                .price(request.price())
                                .totalValue(totalValue)
                                .build();

                transaction = transactionRepository.save(transaction);

                // Update holdings
                updateHolding(request, totalValue);

                // Publish event after successful persistence
                publishTransactionEvent(transaction);

                log.info("Transaction {} completed successfully", transaction.getId());

                return mapToResponse(transaction);
        }

        /**
         * Get portfolio holdings summary for a user.
         */
        @Transactional(readOnly = true)
        public PortfolioResponse getPortfolio(String userId) {
                log.info("Fetching portfolio for user={}", userId);

                List<Holding> holdings = holdingRepository.findByUserId(userId);

                List<PortfolioResponse.HoldingDTO> holdingDTOs = holdings.stream()
                                .map(h -> PortfolioResponse.HoldingDTO.builder()
                                                .symbol(h.getSymbol())
                                                .quantity(h.getQuantity())
                                                .averagePrice(h.getAveragePrice())
                                                .totalInvested(h.getTotalInvested())
                                                .build())
                                .toList();

                BigDecimal totalPortfolioValue = holdings.stream()
                                .map(Holding::getTotalInvested)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return PortfolioResponse.builder()
                                .userId(userId)
                                .holdings(holdingDTOs)
                                .totalPortfolioValue(totalPortfolioValue)
                                .build();
        }

        @Transactional(readOnly = true)
        public Page<TransactionResponse> getTransactions(String userId, Pageable pageable) {
                log.info("Fetching transactions for user={} page={} size={}",
                                userId, pageable.getPageNumber(), pageable.getPageSize());

                return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                                .map(this::mapToResponse);
        }

        /**
         * Update or create a holding based on the transaction type.
         */
        private void updateHolding(TransactionRequest request, BigDecimal totalValue) {
                Holding holding = holdingRepository
                                .findByUserIdAndSymbol(request.userId(), request.symbol().toUpperCase())
                                .orElse(null);

                if (request.type() == TransactionRequest.TransactionTypeDTO.BUY) {
                        if (holding == null) {
                                holding = Holding.builder()
                                                .userId(request.userId())
                                                .symbol(request.symbol().toUpperCase())
                                                .quantity(request.quantity())
                                                .averagePrice(request.price())
                                                .totalInvested(totalValue)
                                                .build();
                        } else {
                                int newQty = holding.getQuantity() + request.quantity();
                                BigDecimal newTotalInvested = holding.getTotalInvested().add(totalValue);
                                BigDecimal newAvgPrice = newTotalInvested.divide(
                                                BigDecimal.valueOf(newQty), 4, RoundingMode.HALF_UP);

                                holding.setQuantity(newQty);
                                holding.setAveragePrice(newAvgPrice);
                                holding.setTotalInvested(newTotalInvested);
                        }
                } else {
                        // SELL
                        if (holding == null || holding.getQuantity() < request.quantity()) {
                                throw new InsufficientHoldingsException(
                                                "Insufficient holdings for " + request.symbol() + ". " +
                                                                "Available: "
                                                                + (holding != null ? holding.getQuantity() : 0) +
                                                                ", Requested: " + request.quantity());
                        }

                        int newQty = holding.getQuantity() - request.quantity();
                        if (newQty == 0) {
                                holdingRepository.delete(holding);
                                return;
                        }

                        BigDecimal newTotalInvested = holding.getAveragePrice()
                                        .multiply(BigDecimal.valueOf(newQty));
                        holding.setQuantity(newQty);
                        holding.setTotalInvested(newTotalInvested);
                }

                holdingRepository.save(holding);
        }

        /**
         * Publish a transaction event to RabbitMQ
         */
        private void publishTransactionEvent(Transaction transaction) {
                TransactionEvent event = TransactionEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .correlationId(MDC.get(CORRELATION_ID_MDC_KEY))
                                .userId(transaction.getUserId())
                                .symbol(transaction.getSymbol())
                                .type(TransactionEvent.TransactionType.valueOf(transaction.getType().name()))
                                .quantity(transaction.getQuantity())
                                .price(transaction.getPrice())
                                .totalValue(transaction.getTotalValue())
                                .timestamp(Instant.now())
                                .build();

                eventPublisher.publish(event);
        }

        private TransactionResponse mapToResponse(Transaction t) {
                return TransactionResponse.builder()
                                .id(t.getId())
                                .userId(t.getUserId())
                                .symbol(t.getSymbol())
                                .type(t.getType().name())
                                .quantity(t.getQuantity())
                                .price(t.getPrice())
                                .totalValue(t.getTotalValue())
                                .createdAt(t.getCreatedAt())
                                .build();
        }
}
