package ru.memearena.user.api; import java.time.Instant; public record GuestSessionResponse(String accessToken, Instant expiresAt) {}
