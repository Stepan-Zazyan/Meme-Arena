# Meme Arena

Meme Arena backend is a Spring Boot modular monolith. The current MVP core implements guest profiles, meme storage by external `imageUrl`, battles, voting with Elo recalculation, and meme leaderboards.

## Implemented MVP core

* Guest user creation and profile lookup under `/api/v1/users`.
* Meme creation and lookup under `/api/v1/memes`; user-submitted memes start as `PENDING`.
* Next battle selection under `/api/v1/battles/next` from `APPROVED` memes only.
* Voting under `/api/v1/votes`, including duplicate-pair protection and Elo updates in one transaction.
* Top memes under `/api/v1/memes/top` for `DAY`, `WEEK`, and `ALL_TIME`.
* Local-only moderation endpoints under `/api/v1/local/memes/{memeId}/approve` and `/reject` when the `local` profile is active.

Physical image upload/storage and the Android/Flutter client are not implemented yet. Memes store only external HTTP/HTTPS image URLs at this stage.

## Manual check in IntelliJ IDEA and Postman

1. Start PostgreSQL using the existing shared IntelliJ Run Configuration.
2. Start the backend using the existing shared IntelliJ Run Configuration with the `local` profile.
3. Import or refresh `postman/Meme Arena.postman_collection.json` and `postman/Local.postman_environment.json` in Postman.
4. Run `Core MVP / Create Guest User`; it saves `userId` to the environment.
5. Run `Core MVP / Get Next Battle`; local Flyway seed data provides approved demo memes and the request saves `leftMemeId`, `rightMemeId`, and `winnerMemeId`.
6. Run `Core MVP / Submit Vote`.
7. Run `Core MVP / Get User Profile` and verify `votesCount` changed.
8. Run `Core MVP / Get Top DAY`, `Get Top WEEK`, or `Get Top ALL_TIME`.
9. Run `Core MVP / Create Meme`; the response is `PENDING` and saves `createdMemeId`.
10. Run `Core MVP / Approve Meme — local only`.
11. Request future battles and verify the approved meme is eligible.

## API docs

OpenAPI is available from the running backend at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

## Media upload and local moderation (backend MVP)

Use IntelliJ IDEA as the primary workflow:

1. Open the shared run configuration **Meme Arena Infrastructure** to start PostgreSQL 17 and MinIO together from `docker-compose.yml`.
2. Start **Meme Arena Backend** with the `local` profile. The backend reads MinIO settings from environment variables and creates the private `meme-images` bucket on startup if it is missing.
3. Open Postman, select the Meme Arena environment, and run the collection requests.

Manual Postman scenario:

1. Run **Users / Create Guest User** and save `userId`.
2. Run **Media / Upload Image**. In the multipart body, keep `userId` from the environment and choose a local JPEG, PNG, WebP, or GIF file in the `file` picker. The response saves `mediaAssetId` and returns a backend `contentUrl`, never a MinIO URL.
3. Run **Memes / Create Meme** with `mediaAssetId` and without `imageUrl`. The new meme is `PENDING` and the response includes `contentUrl`.
4. Run **Moderation / Get Pending Memes**.
5. Run **Moderation / Approve Meme**.
6. Run **Battles / Get Next Battle**.
7. Run **Votes / Submit Vote**.
8. Run **Ranking / Get Top Memes**.

The command line is not required for the normal user path; it is only used by automation and CI.

## Android MVP

Open the monorepo root in IntelliJ IDEA or Android Studio so the shared `.run` configurations are visible. Use **Meme Arena Infrastructure** to start PostgreSQL and MinIO, then **Meme Arena Backend** to run Spring Boot with the local profile. Pick an Android emulator in the device selector and run **Meme Arena Mobile**; it passes `--dart-define=API_BASE_URL=http://10.0.2.2:8080` for emulator access to the host backend.

For a physical Android device, keep the backend running on the development computer and change `API_BASE_URL` in the run configuration to `http://<computer-lan-ip>:8080`. Debug builds allow local cleartext HTTP; release builds do not.

User scenario: create a nickname on onboarding, vote on the funnier meme in **Битва**, open **Топ** and switch День/Неделя/Всё время, choose an image in **Загрузить** and submit it for moderation, then check nickname, votes, submitted memes and status in **Профиль**.

Run backend tests with **Meme Arena Backend Tests** and Flutter tests with **Meme Arena Flutter Tests** from the IDE. Current Android MVP intentionally excludes iOS polishing, web, email/password login, OAuth, push notifications, comments, subscriptions, payments, analytics, admin UI and AI features.

## Secure guest sessions and moderation

Set local backend environment variables in the shared IDEA run configuration or your shell:

* `MEME_ARENA_ADMIN_TOKEN` — local-only admin bearer token placeholder;
* `APP_IP_HASH_PEPPER` — local-only pepper for IP hashing.

Do not commit production secrets. Android creates a guest profile through onboarding, generates and keeps a stable random installationId, stores only the access token in platform secure storage, and stores non-secret `userId`, `nickname`, and `installationId` in SharedPreferences.

Import `postman/Meme Arena.postman_collection.json` and `postman/Local.postman_environment.json`, then fill `adminToken` manually. Protected user requests use `accessToken`; admin moderation requests use `adminToken`.

To test security manually: create a guest, call `/api/v1/users/me`, logout through the profile screen or `POST /api/v1/users/me/logout`, then verify the same token receives 401. Calling protected endpoints without a bearer token should return 401. Moderation is available only under `/api/v1/admin/moderation/memes` with the admin bearer token.

The rate limiter is in-memory and suitable only for one backend instance.

## Мемный скаут

Мемный скаут — репутационная механика, где пользователь получает очки не за массу кликов, а за раннее распознавание мемов, которые позже становятся сильными. При обычном голосовании backend может сохранить `PENDING`-прогноз на выбранный мем, если мем находится в раннем окне после approve и ещё имеет мало битв.

Очки начисляются позже resolver-ом: мем должен накопить достаточно битв и пройти задержку оценки. Успех определяется внутри UTC cohort по Wilson score; для маленьких cohort используется fallback. Local/admin может запустить `POST /api/v1/admin/scout/resolve`; для ручной проверки в local есть `POST /api/v1/admin/local/scout/memes/{memeId}/make-resolvable`.

В Android после раннего голоса показывается «Прогноз сохранён». В профиле отображаются rank, scout points, accuracy, серии, достижения и экран «Мои прогнозы». В Top доступны вкладки «Мемы» и «Скауты» с периодами Неделя/Всё время.

Ограничения MVP: нет турниров, комментариев, push/WebSocket и распределённого scheduler lock; при нескольких backend-инстансах нужно добавить distributed lock.
