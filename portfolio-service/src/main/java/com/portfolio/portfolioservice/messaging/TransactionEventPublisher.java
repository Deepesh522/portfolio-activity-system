package com.portfolio.portfolioservice.messaging;

import com.portfolio.shared.constant.MessagingConstants;
import com.portfolio.shared.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes transaction events to RabbitMQ after successful database commit.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish a transaction event to the portfolio exchange.
     */
    public void publish(TransactionEvent event) {
        try {
            log.info("Publishing transaction event: eventId={} userId={} symbol={} type={} totalValue={}",
                    event.eventId(), event.userId(), event.symbol(), event.type(), event.totalValue());

            rabbitTemplate.convertAndSend(
                    MessagingConstants.PORTFOLIO_EXCHANGE,
                    MessagingConstants.PORTFOLIO_ROUTING_KEY,
                    event);

            log.info("Transaction event published successfully: eventId={}", event.eventId());
        } catch (Exception ex) {
            log.error("Failed to publish transaction event: eventId={} error={}",
                    event.eventId(), ex.getMessage(), ex);
        }
    }
}
