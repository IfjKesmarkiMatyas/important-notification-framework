CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(200),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    kit JSONB,
    rules_hu JSONB,
    rules_en JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE invite_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invite_tokens_hash ON invite_tokens (token_hash);

CREATE TABLE default_kits (
    id SMALLINT PRIMARY KEY,
    kit JSONB NOT NULL,
    rules_hu JSONB NOT NULL,
    rules_en JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE delivery_jobs (
    id UUID PRIMARY KEY,
    purpose VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(500) NOT NULL,
    locale VARCHAR(8) NOT NULL DEFAULT 'hu',
    subject VARCHAR(500),
    body_text TEXT,
    body_html TEXT,
    payload JSONB,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ,
    last_attempt_at TIMESTAMPTZ
);

CREATE INDEX idx_delivery_jobs_status ON delivery_jobs (status, created_at);
