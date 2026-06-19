# Media storage

Meme Arena stores uploaded image bytes in a private MinIO bucket and stores only metadata in PostgreSQL. The bucket is private so credentials, internal object names, and the MinIO endpoint are never exposed to clients.

Clients receive a stable backend URL (`/api/v1/media/{mediaId}/content`) instead of a MinIO URL. This keeps access control, caching headers, ETag handling, and future storage migration behind the backend API.

The storage layer is accessed through `MediaStorage`; the current implementation is `MinioMediaStorage`. A future S3-compatible provider can replace it by adding another implementation without changing controllers or meme business logic.

PostgreSQL and MinIO do not share a transaction. The MVP uses eventual consistency: upload writes the object first and removes it if the DB save fails; cleanup marks old unattached assets as `ORPHANED`, deletes the object, then marks them `DELETED`. A single cleanup failure is logged and does not stop processing the rest.

MVP limitations: no user-facing delete API, no antivirus scanning, no image transcoding, no CDN, local moderation endpoints are profile-gated rather than authenticated, and scheduled orphan cleanup is disabled by default.
