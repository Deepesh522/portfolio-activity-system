package com.portfolio.notificationservice.strategy;

import com.portfolio.shared.event.TransactionEvent;

//Each implementation defines a specific notification trigger condition.

public interface NotificationRuleStrategy {

    /* Evaluate whether this rule matches the given event. */
    boolean matches(TransactionEvent event);

    /* Returns the human-readable name of this rule. */
    String ruleName();

    /* Generate the notification message for a matched event. */
    String generateMessage(TransactionEvent event);
}
