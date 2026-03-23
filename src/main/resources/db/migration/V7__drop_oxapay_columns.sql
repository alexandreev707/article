-- Откат столбцов Oxapay + индекса
DROP INDEX IF EXISTS idx_orders_oxapay_track_id;
ALTER TABLE orders DROP COLUMN IF EXISTS oxapay_track_id;
ALTER TABLE orders DROP COLUMN IF EXISTS oxapay_payment_url;
ALTER TABLE orders DROP COLUMN IF EXISTS oxapay_payout_track_id;

-- Статусы эскроу → прежние значения (VARCHAR)
UPDATE orders SET status = 'CONFIRMED' WHERE status = 'AWAITING_SHIPMENT';
UPDATE orders SET status = 'SHIPPED' WHERE status = 'READY_FOR_RELEASE';
UPDATE orders SET status = 'DELIVERED' WHERE status = 'ESCROW_COMPLETED';
