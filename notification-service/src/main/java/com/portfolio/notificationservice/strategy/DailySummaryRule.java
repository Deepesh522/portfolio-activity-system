package com.portfolio.notificationservice.strategy;

import com.portfolio.shared.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/*
 * Triggers notification for all SELL transactions as a daily summary candidate.
 */
@Component
@Slf4j
public class DailySummaryRule implements NotificationRuleStrategy {

    @Override
    public boolean matches(TransactionEvent event) {
        // Match all SELL transactions for summary tracking
        boolean matched = event.type() == TransactionEvent.TransactionType.SELL;
        if (matched) {
            log.debug("DailySummaryRule matched: userId={} symbol={} type=SELL", event.userId(), event.symbol());
        }
        return matched;
    }

    @Override
    public String ruleName() {
        return "DAILY_SUMMARY";
    }

    @Override
    public String generateMessage(TransactionEvent event) {
        return String.format("Portfolio activity: %s sold %d shares of %s at $%s (Total: $%s)",
                event.userId(), event.quantity(), event.symbol(), event.price(), event.totalValue());
    }
}
