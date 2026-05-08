ALTER TABLE wallet_transactions
    ADD COLUMN IF NOT EXISTS reference_id UUID;

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_reference_id
    ON wallet_transactions(reference_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_wallet_transactions_reference
    ON wallet_transactions(wallet_id, reference_id, type);
