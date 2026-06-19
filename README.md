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
