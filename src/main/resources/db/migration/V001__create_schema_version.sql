-- Placeholder migration to verify Flyway setup
-- This will be replaced with actual schema in later tickets

CREATE TABLE IF NOT EXISTS schema_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO schema_info (version) VALUES ('0.0.1');
