USE sistematambo;

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until DATETIME NULL,
    ADD COLUMN IF NOT EXISTS refresh_token_hash VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS refresh_token_expires_at DATETIME NULL;
