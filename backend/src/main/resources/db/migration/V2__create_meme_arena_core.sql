CREATE TABLE user_profile (
    id UUID PRIMARY KEY,
    nickname VARCHAR(30) NOT NULL,
    votes_count BIGINT NOT NULL DEFAULT 0,
    submitted_memes_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX uk_user_profile_nickname_lower ON user_profile (LOWER(nickname));

CREATE TABLE meme (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    image_url VARCHAR(2000) NOT NULL,
    source_url VARCHAR(2000),
    uploader_id UUID,
    moderation_status VARCHAR(20) NOT NULL,
    rating INTEGER NOT NULL DEFAULT 1500,
    wins BIGINT NOT NULL DEFAULT 0,
    losses BIGINT NOT NULL DEFAULT 0,
    battles_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_meme_uploader FOREIGN KEY (uploader_id) REFERENCES user_profile(id),
    CONSTRAINT chk_meme_moderation_status CHECK (moderation_status IN ('PENDING','APPROVED','REJECTED'))
);

CREATE TABLE vote (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    left_meme_id UUID NOT NULL,
    right_meme_id UUID NOT NULL,
    winner_meme_id UUID NOT NULL,
    loser_meme_id UUID NOT NULL,
    pair_key VARCHAR(73) NOT NULL,
    winner_rating_before INTEGER NOT NULL,
    winner_rating_after INTEGER NOT NULL,
    loser_rating_before INTEGER NOT NULL,
    loser_rating_after INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_vote_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
    CONSTRAINT fk_vote_left_meme FOREIGN KEY (left_meme_id) REFERENCES meme(id),
    CONSTRAINT fk_vote_right_meme FOREIGN KEY (right_meme_id) REFERENCES meme(id),
    CONSTRAINT fk_vote_winner_meme FOREIGN KEY (winner_meme_id) REFERENCES meme(id),
    CONSTRAINT fk_vote_loser_meme FOREIGN KEY (loser_meme_id) REFERENCES meme(id),
    CONSTRAINT uk_vote_user_pair UNIQUE (user_id, pair_key),
    CONSTRAINT chk_vote_distinct_pair CHECK (left_meme_id <> right_meme_id)
);

CREATE INDEX idx_vote_user_id ON vote(user_id);
CREATE INDEX idx_vote_winner_meme_id ON vote(winner_meme_id);
CREATE INDEX idx_vote_loser_meme_id ON vote(loser_meme_id);
CREATE INDEX idx_vote_created_at ON vote(created_at);
CREATE INDEX idx_meme_moderation_status ON meme(moderation_status);
CREATE INDEX idx_meme_rating ON meme(rating);
CREATE INDEX idx_meme_created_at ON meme(created_at);
