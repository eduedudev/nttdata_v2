-- V1__create_tables.sql
-- Account Service Database Schema

-- Customer table (replica from customer-service via Kafka events)
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    identification VARCHAR(50) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Account table
CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'CHECKING')),
    initial_balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Movement table
CREATE TABLE IF NOT EXISTS movements (
    movement_id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    movement_type VARCHAR(20) NOT NULL CHECK (movement_type IN ('CREDIT', 'DEBIT')),
    amount DECIMAL(19, 4) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    description VARCHAR(500),
    account_id BIGINT NOT NULL REFERENCES accounts(account_id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_movements_account_id ON movements(account_id);
CREATE INDEX IF NOT EXISTS idx_movements_date ON movements(date);
CREATE INDEX IF NOT EXISTS idx_movements_account_date ON movements(account_id, date);
