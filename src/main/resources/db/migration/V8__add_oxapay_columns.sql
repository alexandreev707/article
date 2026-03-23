ALTER TABLE orders ADD COLUMN IF NOT EXISTS oxapay_track_id VARCHAR(128);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS oxapay_payment_url TEXT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS oxapay_payout_track_id VARCHAR(128);
CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_oxapay_track_id ON orders(oxapay_track_id) WHERE oxapay_track_id IS NOT NULL;
