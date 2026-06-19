# Admin moderation

Production moderation endpoints use a separate bearer token from `MEME_ARENA_ADMIN_TOKEN`. The token is not stored in Git, the database, the mobile app, or committed Postman environments.

Admins call `/api/v1/admin/moderation/memes` endpoints with `Authorization: Bearer <admin-token>`. The backend performs constant-time comparison and grants `ROLE_ADMIN`; it does not create an admin `UserProfile`.

Later this API token can be replaced by real admin accounts and scoped roles without changing the moderation service methods.
