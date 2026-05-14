CREATE TABLE notifications (
    id         UUID PRIMARY KEY,
    event_id   VARCHAR(255) NOT NULL UNIQUE,
    user_id    VARCHAR(255) NOT NULL,
    channel    VARCHAR(50)  NOT NULL,
    subject    VARCHAR(500) NOT NULL,
    message    VARCHAR(2000) NOT NULL,
    status     VARCHAR(10)  NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    rule_name  VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    sent_at    TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user_id  ON notifications(user_id);
CREATE INDEX idx_notifications_event_id ON notifications(event_id);
