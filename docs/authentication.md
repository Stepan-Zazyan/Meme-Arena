# Authentication

Meme Arena uses opaque guest access tokens instead of JWTs for the MVP so sessions can be revoked immediately and no user claims are trusted from the client. A guest creation response returns the raw token once. PostgreSQL stores only the SHA-256 token hash in `user_session`.

Sessions live for 90 days, are stateless from the HTTP perspective, and are sent as `Authorization: Bearer <accessToken>`. Logout revokes the current session. The MVP allows one active session per guest user.

The guest model intentionally has no password, email, phone, OAuth, or social login. Existing mobile installs with a saved `userId` but no token are migrated back to onboarding; there is no insecure userId-to-token exchange.
