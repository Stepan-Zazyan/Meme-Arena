CREATE TABLE media_asset (
    id UUID PRIMARY KEY,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    original_filename VARCHAR(255),
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    uploaded_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_media_asset_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES user_profile(id),
    CONSTRAINT chk_media_asset_status CHECK (status IN ('ACTIVE','ORPHANED','DELETED'))
);
CREATE INDEX idx_media_asset_sha256 ON media_asset(sha256);
CREATE INDEX idx_media_asset_uploaded_by ON media_asset(uploaded_by);
CREATE INDEX idx_media_asset_status ON media_asset(status);
CREATE INDEX idx_media_asset_created_at ON media_asset(created_at);

ALTER TABLE meme ADD COLUMN media_asset_id UUID;
ALTER TABLE meme ADD COLUMN moderated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE meme ADD COLUMN moderation_reason VARCHAR(500);
ALTER TABLE meme ALTER COLUMN image_url DROP NOT NULL;
ALTER TABLE meme ADD CONSTRAINT fk_meme_media_asset FOREIGN KEY (media_asset_id) REFERENCES media_asset(id);
CREATE UNIQUE INDEX uk_meme_media_asset_id ON meme(media_asset_id) WHERE media_asset_id IS NOT NULL;
CREATE INDEX idx_meme_media_asset_id ON meme(media_asset_id);
