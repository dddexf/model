-- Replace api_key_cipher with actual AES ciphertext from backend AesKeyCipher.
INSERT INTO ai_provider (name, base_url, api_key_cipher, enabled)
VALUES ('DeepSeek', 'https://api.deepseek.com/v1', 'REPLACE_ME_WITH_AES_CIPHER', 1);

INSERT INTO ai_model (provider_id, model_code, display_name, max_context_tokens, enabled, sort_no)
VALUES
    (1, 'deepseek-chat', 'DeepSeek Chat', 16000, 1, 1),
    (1, 'deepseek-reasoner', 'DeepSeek Reasoner', 16000, 1, 2);
