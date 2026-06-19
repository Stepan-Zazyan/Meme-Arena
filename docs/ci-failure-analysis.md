# CI failure analysis

GitHub CLI is not installed in this container, so remote GitHub Actions logs for commit `03c3dba95660020170c075b8cbfea18f110e960a` could not be fetched. I analyzed the workflow definitions and reproduced the available local equivalents. Local Maven dependency resolution is blocked by the environment with HTTP 403 from Maven Central, and Docker/Flutter are not installed in this container; those limitations are recorded below.

## Check 1: Backend CI / backend / Compile

- Workflow: `.github/workflows/backend.yml`.
- Job: `backend`.
- Failed step: `Compile`.
- Main error: merged backend code now requires authenticated `CurrentUser` for meme creation, battles, votes, and media upload, while integration tests still exercised the deprecated query/body `userId` path and expected the old top-level guest response shape.
- Root cause: merge introduced auth/session response changes, but shared integration tests were not updated.
- Fix: updated core and media integration tests to parse `user.id` and `accessToken`, and to send `Authorization: Bearer <token>` for authenticated endpoints.
- Recheck result: local Maven compile could not complete because Maven Central access is blocked in this container: `Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.5.14 ... 403 Forbidden`.

## Check 2: Backend CI / backend / Unit and integration tests

- Workflow: `.github/workflows/backend.yml`.
- Job: `backend`.
- Failed step: `Unit and integration tests`.
- Main error: expected JSON paths and auth mode in `CoreMvpIntegrationTests` and `MediaIntegrationTests` no longer matched the merged API contract.
- Root cause: merge changed guest creation from top-level user fields to `{ user, accessToken, expiresAt }` and protected endpoints now derive the user from the bearer session.
- Fix: updated tests to use the merged auth contract without disabling assertions or removing integration coverage.
- Recheck result: local full Maven test run is blocked by Maven Central 403 and missing Docker/Testcontainers runtime in this container.

## Check 3: Flutter CI / flutter / Analyze or Tests

- Workflow: `.github/workflows/flutter.yml`.
- Job: `flutter`.
- Failed step: likely `Tests` if the app was still tested against the old guest response; `Analyze` was also reviewed by source inspection.
- Main error: mobile code already expects the merged `{ user, accessToken }` response and stores the token, but remote logs were unavailable to confirm the exact Flutter step.
- Root cause: remote GitHub logs are unavailable locally; no stale top-level guest parsing remained in the mobile repository code.
- Fix: no mobile code change was required after inspection.
- Recheck result: Flutter commands could not run because `flutter` is not installed in this container.

## Check 4: Flutter CI / flutter / Build debug APK

- Workflow: `.github/workflows/flutter.yml`.
- Job: `flutter`.
- Failed step: `Build debug APK`.
- Main error: remote logs unavailable; Android Gradle configuration was inspected for namespace, applicationId, compileSdk/minSdk, plugin versions, manifest, and debug define usage.
- Root cause: not confirmed from remote logs. The workflow passes `--dart-define=API_BASE_URL=http://10.0.2.2:8080` and does not require release signing secrets for debug.
- Fix: no Android code change was required after inspection.
- Recheck result: debug APK build could not run because Flutter/Android tooling is not installed in this container.

## Additional checks

- Conflict markers: no merge conflict markers were found in tracked source files.
- Flyway versions: root migrations were checked and have no duplicate version number.
- Docker Compose: inspected; local PostgreSQL and MinIO services are present for development, while CI relies on Testcontainers.
- Testcontainers: backend tests use PostgreSQL Testcontainers. MinIO integration uses the local/in-memory storage profile in tests rather than requiring production secrets.
