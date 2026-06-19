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
