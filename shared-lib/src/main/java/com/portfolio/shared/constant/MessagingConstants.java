package com.portfolio.shared.constant;

public final class MessagingConstants {

    private MessagingConstants() {
    }

    // Exchange
    public static final String PORTFOLIO_EXCHANGE = "portfolio.exchange";
    public static final String PORTFOLIO_ROUTING_KEY = "portfolio.transaction";

    // Notification queues
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_RETRY_QUEUE = "notification.retry.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    // Retry configuration
    public static final int MAX_RETRY_COUNT = 3;
    public static final long RETRY_INITIAL_INTERVAL_MS = 5000;
}
