-- Tabela principal de registros de inventário/prevenção de perdas
CREATE TABLE IF NOT EXISTS registers (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    color VARCHAR(255),
    location VARCHAR(255),
    sku VARCHAR(255),
    floor VARCHAR(255),
    street VARCHAR(255),
    position VARCHAR(255),
    sub_position VARCHAR(255),
    identifier_id VARCHAR(255),
    seller_id VARCHAR(255),
    meli_id VARCHAR(255),
    sale_price NUMERIC(19, 2),
    compensation_price NUMERIC(19, 2),
    protocol VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(50),
    CONSTRAINT fk_registers_user FOREIGN KEY (user_id) REFERENCES users(id)
);
