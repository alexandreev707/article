-- Product status (DRAFT, PUBLISHED) for workflow
ALTER TABLE products ADD COLUMN status VARCHAR(20) DEFAULT 'DRAFT';
UPDATE products SET status = 'PUBLISHED' WHERE active = true;
UPDATE products SET status = 'DRAFT' WHERE active = false;

-- Bank account for seller payouts
ALTER TABLE users ADD COLUMN bank_account VARCHAR(255);
