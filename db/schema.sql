CREATE TABLE IF NOT EXISTS ai_provider (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    api_key_cipher VARCHAR(2048) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    model_code VARCHAR(100) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    max_context_tokens INT NOT NULL DEFAULT 8000,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    sort_no INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_model_provider_code UNIQUE (provider_id, model_code),
    CONSTRAINT fk_model_provider FOREIGN KEY (provider_id) REFERENCES ai_provider(id)
);

CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    last_model_id BIGINT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content LONGTEXT NOT NULL,
    seq_no INT NOT NULL,
    model_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_message_seq UNIQUE (session_id, seq_no),
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES chat_session(id)
);

CREATE INDEX idx_session_updated_at ON chat_session(updated_at);
CREATE INDEX idx_message_session_seq ON chat_message(session_id, seq_no);
CREATE INDEX idx_model_sort ON ai_model(sort_no, id);
