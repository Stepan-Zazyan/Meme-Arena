CREATE TABLE app_metadata (
    id BIGSERIAL PRIMARY KEY,
    property_key VARCHAR(100) NOT NULL UNIQUE,
    property_value VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
INSERT INTO app_metadata (property_key, property_value, created_at)
VALUES ('schema_version', '1', CURRENT_TIMESTAMP);
