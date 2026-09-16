CREATE TABLE scrape_runs (
    id UUID PRIMARY KEY,
    source_id VARCHAR(40) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    fetched INT NOT NULL DEFAULT 0,
    normalized INT NOT NULL DEFAULT 0,
    error_message TEXT
);

CREATE INDEX idx_scrape_runs_source_started ON scrape_runs (source_id, started_at DESC);

CREATE TABLE raw_intakes (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES scrape_runs (id) ON DELETE CASCADE,
    source_id VARCHAR(40) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,
    http_status INT,
    content_type VARCHAR(200),
    body TEXT,
    error_message TEXT
);

CREATE TABLE normalized_events (
    id UUID PRIMARY KEY,
    family VARCHAR(20) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    external_id VARCHAR(500) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL,
    locale VARCHAR(8) NOT NULL,
    headline TEXT NOT NULL,
    summary TEXT,
    canonical_url TEXT,
    payload JSONB NOT NULL,
    raw_intake_id UUID REFERENCES raw_intakes (id),
    UNIQUE (source_id, external_id)
);

CREATE INDEX idx_normalized_events_ingested ON normalized_events (ingested_at DESC);
CREATE INDEX idx_normalized_events_family ON normalized_events (family, ingested_at DESC);

CREATE TABLE market_snapshots (
    instrument VARCHAR(80) PRIMARY KEY,
    price_usd DOUBLE PRECISION,
    price_huf DOUBLE PRECISION,
    seen_at TIMESTAMPTZ NOT NULL
);
