CREATE TABLE products(
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL ,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    price_in_cents BIGINT NOT NULL CHECK (price_in_cents >= 0),
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(2048),
    category_id UUID NOT NULL REFERENCES category(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
)