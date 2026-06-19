# Meme Arena

Meme Arena is a Spring Boot backend MVP for meme battles.

## Backend MVP scope

Implemented now:

* create a guest user by nickname;
* create a meme with an external `imageUrl`;
* approve a meme in the `local` profile;
* get the next battle pair from approved memes;
* vote for one meme in a pair;
* update Elo rating, wins, losses, and battles count transactionally;
* get top memes for `ALL_TIME`.

Not implemented in this stage:

* file uploads / MinIO storage;
* Flutter / Android client;
* authentication flows and production moderation UI;
* Redis, Kafka, microservices, or AI features.

## Run backend through IntelliJ IDEA

1. Open the repository root in IntelliJ IDEA.
2. Make sure `backend/pom.xml` is imported as a Maven project.
3. Start PostgreSQL for local development.
4. Run the Spring Boot application from `ru.memearena.MemeArenaApplication` with the `local` profile.
5. Open Swagger UI at `http://localhost:8080/swagger-ui.html` or OpenAPI JSON at `http://localhost:8080/v3/api-docs`.

## Check MVP through Postman

1. Import `postman/Meme Arena.postman_collection.json`.
2. Import `postman/Local.postman_environment.json` and select it.
3. Run requests in order:
   * `Create Guest User` saves `userId`;
   * `Create Meme` saves `memeId`;
   * `Approve Meme` changes that meme to `APPROVED` in local mode;
   * `Get Next Battle` saves `leftMemeId` and `rightMemeId`;
   * `Submit Vote` votes for `leftMemeId`;
   * `Get Top` returns the sorted leaderboard.

Local Flyway seed data inserts eight approved demo memes, so battles are available immediately after startup with the `local` profile.

## Android / Flutter MVP

A Flutter Android MVP is available in `mobile/`. It uses the existing backend contracts from the Spring controllers/Postman collection: guest users, battles, votes, top memes, media upload when available, and `imageUrl` meme creation as fallback.

### Open in Android Studio / IntelliJ IDEA

1. Install Flutter stable and Android SDK.
2. Open the `mobile/` directory as a Flutter project.
3. Run `flutter pub get` from `mobile/`.
4. Select an Android emulator or physical Android device.

### Configure API_BASE_URL

The app reads the backend URL from Dart defines:

```bash
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

If the define is omitted, Android emulator default is `http://10.0.2.2:8080`. For a physical device, use a LAN URL reachable from the phone, for example:

```bash
flutter run --dart-define=API_BASE_URL=http://192.168.1.10:8080
```

### Run on emulator

1. Start the backend locally on port `8080` with the `local` profile.
2. Start an Android emulator.
3. From `mobile/`, run:

```bash
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

Debug builds allow cleartext HTTP through `mobile/android/app/src/debug/AndroidManifest.xml`; release keeps cleartext disabled.

### Run on physical Android

1. Connect the device and enable USB debugging.
2. Make sure backend host and phone are on the same network.
3. Use the computer LAN IP as `API_BASE_URL`:

```bash
flutter run --dart-define=API_BASE_URL=http://<YOUR_LAN_IP>:8080
```

### Main scenario checklist

1. Enter a nickname on first launch; the app calls `POST /api/v1/users/guest` and stores `userId`/nickname locally.
2. Open **Битва**; the app calls `GET /api/v1/battles/next?userId=...` and shows two meme cards or an empty state for `204`.
3. Tap the funnier meme; the app calls `POST /api/v1/votes`, shows `Голос учтён`, then loads the next pair.
4. Open **Топ**; the app calls `GET /api/v1/memes/top?period=ALL_TIME&limit=20` and supports pull-to-refresh.
5. Open **Загрузить**; choose an image and title. The app first tries `POST /api/v1/media/images` multipart with `userId` and `file`; if upload is not available it can submit a fallback `imageUrl` to `POST /api/v1/memes`.
6. Open **Профиль**; the app calls `GET /api/v1/users/{userId}` and shows nickname, votes count, submitted memes count, and status when present.
7. Use **Сменить пользователя** to clear local storage and return to onboarding.

### Current limitations

* No auth, push notifications, comments, tournaments, scouts UI, payments, ads, Firebase, Supabase, WebView, Bloc, Redux, or GetX.
* Upload depends on backend media endpoint and environment storage configuration; the app keeps an `imageUrl` fallback form for local MVP use.
* Image selection is gallery-only via Android Photo Picker / platform picker.
