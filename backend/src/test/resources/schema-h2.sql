DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_session;
DROP TABLE IF EXISTS ai_model;
DROP TABLE IF EXISTS ai_provider;

CREATE TABLE ai_provider (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    api_key_cipher VARCHAR(2048) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    model_code VARCHAR(100) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    max_context_tokens INT NOT NULL DEFAULT 8000,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_no INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_model_provider_code UNIQUE (provider_id, model_code),
    CONSTRAINT fk_model_provider FOREIGN KEY (provider_id) REFERENCES ai_provider(id)
);

CREATE TABLE chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    last_model_id BIGINT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content CLOB NOT NULL,
    seq_no INT NOT NULL,
    model_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_message_seq UNIQUE (session_id, seq_no),
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES chat_session(id)
);
