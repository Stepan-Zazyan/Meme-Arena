ALTER TABLE meme ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE meme ADD COLUMN approval_cohort_date DATE;
UPDATE meme SET approved_at = COALESCE(moderated_at, created_at), approval_cohort_date = CAST(COALESCE(moderated_at, created_at) AT TIME ZONE 'UTC' AS DATE) WHERE moderation_status='APPROVED';
CREATE INDEX idx_meme_approval_cohort_date ON meme(approval_cohort_date);
CREATE INDEX idx_meme_approved_at ON meme(approved_at);

CREATE TABLE meme_prediction (
 id UUID PRIMARY KEY, user_id UUID NOT NULL, meme_id UUID NOT NULL, vote_id UUID NOT NULL, status VARCHAR(20) NOT NULL,
 battles_before_vote BIGINT NOT NULL, meme_rating_before_vote INTEGER NOT NULL, predicted_at TIMESTAMP WITH TIME ZONE NOT NULL,
 resolved_at TIMESTAMP WITH TIME ZONE, points_awarded INTEGER NOT NULL DEFAULT 0, resolution_reason VARCHAR(500),
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_prediction_user FOREIGN KEY(user_id) REFERENCES user_profile(id),
 CONSTRAINT fk_prediction_meme FOREIGN KEY(meme_id) REFERENCES meme(id),
 CONSTRAINT fk_prediction_vote FOREIGN KEY(vote_id) REFERENCES vote(id),
 CONSTRAINT uk_prediction_user_meme UNIQUE(user_id,meme_id), CONSTRAINT uk_prediction_vote UNIQUE(vote_id),
 CONSTRAINT chk_prediction_status CHECK(status IN ('PENDING','SUCCESS','FAILED','EXPIRED'))
);
CREATE INDEX idx_meme_prediction_status ON meme_prediction(status);
CREATE INDEX idx_meme_prediction_meme_id ON meme_prediction(meme_id);
CREATE INDEX idx_meme_prediction_user_id ON meme_prediction(user_id);
CREATE INDEX idx_meme_prediction_predicted_at ON meme_prediction(predicted_at);
CREATE INDEX idx_meme_prediction_resolved_at ON meme_prediction(resolved_at);

CREATE TABLE user_scout_stats (
 user_id UUID PRIMARY KEY, scout_points BIGINT NOT NULL DEFAULT 0, predictions_count BIGINT NOT NULL DEFAULT 0,
 successful_predictions BIGINT NOT NULL DEFAULT 0, failed_predictions BIGINT NOT NULL DEFAULT 0, expired_predictions BIGINT NOT NULL DEFAULT 0,
 current_success_streak INTEGER NOT NULL DEFAULT 0, best_success_streak INTEGER NOT NULL DEFAULT 0,
 last_resolved_prediction_at TIMESTAMP WITH TIME ZONE, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_user_scout_stats_user FOREIGN KEY(user_id) REFERENCES user_profile(id)
);
INSERT INTO user_scout_stats(user_id,updated_at) SELECT id, now() FROM user_profile ON CONFLICT DO NOTHING;

CREATE TABLE user_achievement (
 id UUID PRIMARY KEY, user_id UUID NOT NULL, achievement_code VARCHAR(50) NOT NULL, unlocked_at TIMESTAMP WITH TIME ZONE NOT NULL, metadata_json TEXT,
 CONSTRAINT fk_user_achievement_user FOREIGN KEY(user_id) REFERENCES user_profile(id), CONSTRAINT uk_user_achievement UNIQUE(user_id,achievement_code)
);
CREATE INDEX idx_user_achievement_user_id ON user_achievement(user_id);

CREATE TABLE scout_point_event (
 id UUID PRIMARY KEY, user_id UUID NOT NULL, prediction_id UUID, points INTEGER NOT NULL, event_type VARCHAR(30) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT fk_scout_point_event_user FOREIGN KEY(user_id) REFERENCES user_profile(id), CONSTRAINT fk_scout_point_event_prediction FOREIGN KEY(prediction_id) REFERENCES meme_prediction(id),
 CONSTRAINT chk_scout_point_event_type CHECK(event_type IN ('PREDICTION_SUCCESS','ADMIN_ADJUSTMENT'))
);
CREATE INDEX idx_scout_point_event_user_created ON scout_point_event(user_id,created_at);
CREATE INDEX idx_scout_point_event_created_at ON scout_point_event(created_at);
