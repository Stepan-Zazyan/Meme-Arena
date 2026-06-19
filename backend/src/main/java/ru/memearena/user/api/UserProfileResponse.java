package ru.memearena.user.api;

import ru.memearena.scout.api.ScoutDtos.AchievementResponse;
import ru.memearena.scout.api.ScoutDtos.ScoutStatsResponse;
import ru.memearena.user.domain.UserProfile;
import ru.memearena.user.domain.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String nickname,
    long votesCount,
    long submittedMemesCount,
    @Deprecated UserStatus status,
    ScoutStatsResponse scout,
    List<AchievementResponse> achievements,
    Instant createdAt
) {
    public static UserProfileResponse from(UserProfile user) {
        return from(user, null, List.of());
    }

    public static UserProfileResponse from(
        UserProfile user,
        ScoutStatsResponse scout,
        List<AchievementResponse> achievements
    ) {
        return new UserProfileResponse(
            user.getId(),
            user.getNickname(),
            user.getVotesCount(),
            user.getSubmittedMemesCount(),
            user.status(),
            scout,
            achievements == null ? List.of() : List.copyOf(achievements),
            user.getCreatedAt()
        );
    }
}
