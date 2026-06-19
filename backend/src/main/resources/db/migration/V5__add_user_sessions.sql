CREATE TABLE user_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    client_installation_id VARCHAR(100),
    created_ip_hash VARCHAR(64),
    CONSTRAINT fk_user_session_user FOREIGN KEY (user_id) REFERENCES user_profile(id)
);
CREATE INDEX idx_user_session_user_id ON user_session(user_id);
CREATE INDEX idx_user_session_expires_at ON user_session(expires_at);
CREATE INDEX idx_user_session_revoked_at ON user_session(revoked_at);
CREATE UNIQUE INDEX uk_user_session_one_active_per_user ON user_session(user_id) WHERE revoked_at IS NULL;
