package ru.memearena.user.api; import java.time.Instant; public record CreateGuestUserResponse(UserProfileResponse user, String accessToken, Instant expiresAt) {}
