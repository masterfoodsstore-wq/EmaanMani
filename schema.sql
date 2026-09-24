-- ==============================================================================
-- Enterprise Secure Payment, Auth, Deposit, Withdrawal, & Ledger Schema
-- PostgreSQL 14+ / MySQL 8.0+ Compatible Dialect
-- ==============================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    mobile VARCHAR(32) NOT NULL UNIQUE,
    whatsapp_number VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    whatsapp_verified BOOLEAN DEFAULT FALSE,
    account_status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'BLOCKED')),
    balance NUMERIC(16, 2) DEFAULT 0.00 NOT NULL CHECK (balance >= 0.00),
    reserved_balance NUMERIC(16, 2) DEFAULT 0.00 NOT NULL CHECK (reserved_balance >= 0.00),
    referral_code VARCHAR(32),
    referred_by VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_mobile ON users(mobile);
CREATE INDEX idx_users_status ON users(account_status);

-- OTP Verifications
CREATE TABLE IF NOT EXISTS otp_verifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    mobile VARCHAR(32) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attempts INT DEFAULT 0 NOT NULL,
    max_attempts INT DEFAULT 5 NOT NULL,
    is_used BOOLEAN DEFAULT FALSE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_otp_mobile ON otp_verifications(mobile, is_used);

-- Payment Providers & Merchant Config
CREATE TABLE IF NOT EXISTS payment_providers (
    id VARCHAR(32) PRIMARY KEY, -- 'easypaisa', 'jazzcash', 'fastpay'
    name VARCHAR(64) NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    account_title VARCHAR(128) NOT NULL,
    account_number VARCHAR(64) NOT NULL,
    merchant_id VARCHAR(128),
    api_key_encrypted TEXT,
    secret_key_encrypted TEXT,
    webhook_secret VARCHAR(255),
    min_deposit NUMERIC(12, 2) DEFAULT 100.00 NOT NULL,
    max_deposit NUMERIC(12, 2) DEFAULT 50000.00 NOT NULL,
    min_withdrawal NUMERIC(12, 2) DEFAULT 200.00 NOT NULL,
    max_withdrawal NUMERIC(12, 2) DEFAULT 25000.00 NOT NULL,
    deposit_fee_percentage NUMERIC(5, 2) DEFAULT 0.00 NOT NULL,
    withdrawal_fee_fixed NUMERIC(12, 2) DEFAULT 15.00 NOT NULL,
    withdrawal_fee_percentage NUMERIC(5, 2) DEFAULT 1.00 NOT NULL,
    instructions TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Deposits
CREATE TABLE IF NOT EXISTS deposits (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    payment_method VARCHAR(32) NOT NULL REFERENCES payment_providers(id),
    amount NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    fee NUMERIC(14, 2) DEFAULT 0.00 NOT NULL,
    net_credited NUMERIC(14, 2) NOT NULL,
    sender_number VARCHAR(32) NOT NULL,
    transaction_reference VARCHAR(128) NOT NULL UNIQUE,
    proof_url TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    admin_note TEXT,
    approved_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_deposits_user ON deposits(user_id);
CREATE INDEX idx_deposits_status ON deposits(status);
CREATE INDEX idx_deposits_ref ON deposits(transaction_reference);

-- Withdrawals
CREATE TABLE IF NOT EXISTS withdrawals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    payment_method VARCHAR(32) NOT NULL REFERENCES payment_providers(id),
    amount NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    fee NUMERIC(14, 2) DEFAULT 0.00 NOT NULL,
    net_amount NUMERIC(14, 2) NOT NULL,
    account_name VARCHAR(128) NOT NULL,
    account_number VARCHAR(64) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'PAID', 'REJECTED', 'CANCELLED')),
    transaction_reference VARCHAR(128),
    admin_note TEXT,
    processed_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_withdrawals_user ON withdrawals(user_id);
CREATE INDEX idx_withdrawals_status ON withdrawals(status);

-- Financial Ledger (Immutable Double-Entry Style Transaction Log)
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    type VARCHAR(32) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL_LOCK', 'WITHDRAWAL_PAID', 'WITHDRAWAL_REFUND', 'ADJUSTMENT')),
    reference_type VARCHAR(32) NOT NULL CHECK (reference_type IN ('DEPOSIT', 'WITHDRAWAL', 'ADMIN_ADJUSTMENT')),
    reference_id BIGINT NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    balance_before NUMERIC(14, 2) NOT NULL,
    balance_after NUMERIC(14, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'SUCCESS' NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_user ON transactions(user_id);

-- Administrators
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) DEFAULT 'SUPER_ADMIN' NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- Immutable Audit Log
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_id BIGINT NOT NULL,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_admin ON audit_logs(admin_id);
