CREATE TABLE holdings (
    id             UUID PRIMARY KEY,
    user_id        VARCHAR(255)  NOT NULL,
    symbol         VARCHAR(10)   NOT NULL,
    quantity       INT           NOT NULL CHECK (quantity >= 0),
    average_price  NUMERIC(19,4) NOT NULL,
    total_invested NUMERIC(19,4) NOT NULL,
    version        BIGINT        NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_holdings_user_symbol UNIQUE (user_id, symbol)
);
