ALTER TABLE products
    ALTER COLUMN price_in_cents TYPE NUMERIC(12, 2)
        USING (price_in_cents::NUMERIC / 100);

ALTER TABLE products
    RENAME COLUMN price_in_cents TO price;