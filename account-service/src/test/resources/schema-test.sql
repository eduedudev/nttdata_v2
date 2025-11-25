-- Test schema for H2
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    identification VARCHAR(50) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    initial_balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE IF NOT EXISTS movements (
    movement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    movement_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    description VARCHAR(500),
    account_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
