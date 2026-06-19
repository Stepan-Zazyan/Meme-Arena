CREATE TABLE product_event (
    id UUID PRIMARY KEY,
    user_id UUID,
    installation_id VARCHAR(100),
    event_type VARCHAR(50) NOT NULL,
    source VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    app_version VARCHAR(30),
    platform VARCHAR(20),
    CONSTRAINT fk_product_event_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
    CONSTRAINT chk_product_event_type CHECK (event_type IN ('APP_OPENED','ONBOARDING_COMPLETED','BATTLE_OPENED','VOTE_COMPLETED','TOP_OPENED','MEME_UPLOAD_COMPLETED','PROFILE_OPENED','SCOUT_PREDICTIONS_OPENED','TOURNAMENT_OPENED','TOURNAMENT_VOTE_COMPLETED')),
    CONSTRAINT chk_product_event_source CHECK (source IN ('ANDROID','BACKEND'))
);
CREATE INDEX idx_product_event_received_at ON product_event(received_at);
CREATE INDEX idx_product_event_user_received ON product_event(user_id, received_at);
CREATE INDEX idx_product_event_type_received ON product_event(event_type, received_at);
