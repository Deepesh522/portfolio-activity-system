CREATE TABLE notification_preferences (
    id                   UUID PRIMARY KEY,
    user_id              VARCHAR(255) NOT NULL UNIQUE,
    email_enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    sms_enabled          BOOLEAN      NOT NULL DEFAULT FALSE,
    webhook_enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    watched_symbols      VARCHAR(1000),
    high_value_threshold DOUBLE PRECISION NOT NULL DEFAULT 10000.0
);

-- Seed default preferences for demo users
INSERT INTO notification_preferences (id, user_id, email_enabled, sms_enabled, webhook_enabled, watched_symbols, high_value_threshold)
VALUES
    (gen_random_uuid(), 'u1', true, false, false, 'AAPL,GOOGL,TSLA', 10000.0),
    (gen_random_uuid(), 'u2', true, true, false, 'MSFT,AMZN', 5000.0);
