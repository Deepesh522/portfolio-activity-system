package com.portfolio.notificationservice.strategy;

import com.portfolio.notificationservice.entity.NotificationPreference;
import com.portfolio.notificationservice.repository.NotificationPreferenceRepository;
import com.portfolio.shared.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

//Triggers notification when a transaction involves a symbol on the user's watchlist.

@Component
@RequiredArgsConstructor
@Slf4j
public class StockWatchRule implements NotificationRuleStrategy {

    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    public boolean matches(TransactionEvent event) {
        return preferenceRepository.findByUserId(event.userId())
                .map(pref -> {
                    if (pref.getWatchedSymbols() == null || pref.getWatchedSymbols().isBlank()) {
                        return false;
                    }
                    Set<String> watchedSymbols = Arrays.stream(pref.getWatchedSymbols().split(","))
                            .map(String::trim)
                            .map(String::toUpperCase)
                            .collect(Collectors.toSet());

                    boolean matched = watchedSymbols.contains(event.symbol().toUpperCase());
                    if (matched) {
                        log.debug("StockWatchRule matched: userId={} symbol={}", event.userId(), event.symbol());
                    }
                    return matched;
                })
                .orElse(false);
    }

    @Override
    public String ruleName() {
        return "STOCK_WATCH";
    }

    @Override
    public String generateMessage(TransactionEvent event) {
        return String.format("Watchlist alert: %s executed a %s for %d shares of %s at $%s",
                event.userId(), event.type(), event.quantity(), event.symbol(), event.price());
    }
}
