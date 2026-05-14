CREATE TABLE IF NOT EXISTS eval_runs (
    id BIGSERIAL PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL REFERENCES knowledge_bases(id),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    total_cases INT NOT NULL DEFAULT 0,
    hit_count INT NOT NULL DEFAULT 0,
    hit_rate DOUBLE PRECISION,
    mrr DOUBLE PRECISION,
    avg_latency_ms BIGINT,
    avg_score DOUBLE PRECISION,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS eval_cases (
    id BIGSERIAL PRIMARY KEY,
    eval_run_id BIGINT NOT NULL REFERENCES eval_runs(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    expected_answer TEXT,
    expected_sources JSONB,
    actual_answer TEXT,
    actual_sources JSONB,
    retrieval_hit BOOLEAN,
    retrieval_rank INT,
    score DOUBLE PRECISION,
    latency_ms BIGINT,
    error_message TEXT
);

CREATE INDEX IF NOT EXISTS idx_eval_cases_run_id ON eval_cases(eval_run_id);
