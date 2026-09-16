CREATE TABLE decision_settings (
    id SMALLINT PRIMARY KEY,
    mode VARCHAR(20) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

INSERT INTO decision_settings (id, mode, updated_at) VALUES (1, 'native', NOW());

CREATE TABLE decision_runs (
    id UUID PRIMARY KEY,
    event_id UUID REFERENCES normalized_events (id),
    engine VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    evaluated INT NOT NULL DEFAULT 0,
    fired INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_decision_runs_event ON decision_runs (event_id);

CREATE TABLE user_decisions (
    id UUID PRIMARY KEY,
    run_id UUID REFERENCES decision_runs (id),
    user_id UUID NOT NULL REFERENCES users (id),
    event_id UUID REFERENCES normalized_events (id),
    source_id VARCHAR(40) NOT NULL,
    external_id VARCHAR(500) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    level VARCHAR(20),
    reason TEXT,
    engine VARCHAR(20) NOT NULL,
    error_message TEXT,
    kit_snapshot JSONB,
    channels JSONB,
    delivery_job_ids JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, source_id, external_id)
);

CREATE INDEX idx_user_decisions_event ON user_decisions (event_id);
CREATE INDEX idx_user_decisions_user ON user_decisions (user_id, created_at DESC);
