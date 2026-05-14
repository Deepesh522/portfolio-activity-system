CREATE TABLE failed_notifications (
    id             UUID PRIMARY KEY,
    event_id       VARCHAR(255)  NOT NULL,
    user_id        VARCHAR(255)  NOT NULL,
    payload        VARCHAR(2000) NOT NULL,
    failure_reason VARCHAR(1000) NOT NULL,
    retry_count    INT           NOT NULL DEFAULT 0,
    failed_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
