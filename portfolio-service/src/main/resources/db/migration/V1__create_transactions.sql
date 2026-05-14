CREATE TABLE transactions (
    id          UUID PRIMARY KEY,
    user_id     VARCHAR(255) NOT NULL,
    symbol      VARCHAR(10)  NOT NULL,
    type        VARCHAR(4)   NOT NULL CHECK (type IN ('BUY', 'SELL')),
    quantity    INT          NOT NULL CHECK (quantity > 0),
    price       NUMERIC(19,4) NOT NULL CHECK (price > 0),
    total_value NUMERIC(19,4) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_symbol  ON transactions(symbol);
