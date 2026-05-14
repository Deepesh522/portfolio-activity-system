package com.portfolio.notificationservice.strategy;

import com.portfolio.notificationservice.entity.NotificationPreference;
import com.portfolio.notificationservice.repository.NotificationPreferenceRepository;
import com.portfolio.shared.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Triggers notification when a BUY transaction exceeds the user's high-value
 * threshold.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HighValueTransactionRule implements NotificationRuleStrategy {

    private static final double DEFAULT_THRESHOLD = 10_000.0;
    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    public boolean matches(TransactionEvent event) {
        if (event.type() != TransactionEvent.TransactionType.BUY) {
            return false;
        }

        double threshold = preferenceRepository.findByUserId(event.userId())
                .map(NotificationPreference::getHighValueThreshold)
                .orElse(DEFAULT_THRESHOLD);

        boolean matched = event.totalValue().doubleValue() > threshold;

        if (matched) {
            log.debug("HighValueTransactionRule matched: userId={} totalValue={} threshold={}",
                    event.userId(), event.totalValue(), threshold);
        }

        return matched;
    }

    @Override
    public String ruleName() {
        return "HIGH_VALUE_TRANSACTION";
    }

    @Override
    public String generateMessage(TransactionEvent event) {
        return String.format("High-value BUY alert: %s purchased %d shares of %s for $%s (Total: %s)",
                event.userId(), event.quantity(), event.symbol(), event.price(), event.totalValue());
    }
}
