package ru.memearena.user.api;

import ru.memearena.scout.api.ScoutDtos.AchievementResponse;
import ru.memearena.scout.api.ScoutDtos.ScoutStatsResponse;
import ru.memearena.scout.application.AchievementService;
import ru.memearena.scout.domain.ScoutRank;
import ru.memearena.scout.domain.UserAchievement;
import ru.memearena.scout.domain.UserScoutStats;
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
        long scoutPoints,
        ScoutRank rank,
        long predictionsCount,
        long successfulPredictions,
        long failedPredictions,
        long expiredPredictions,
        Double accuracy,
        int currentSuccessStreak,
        int bestSuccessStreak,
        ScoutStatsResponse scout,
        List<AchievementResponse> achievements,
        Instant createdAt
) {
    public static UserProfileResponse from(UserProfile u) {
        return from(u, null, List.of());
    }

    public static UserProfileResponse from(UserProfile u, UserScoutStats stats, List<UserAchievement> achievements) {
        long points = stats == null ? 0 : stats.getScoutPoints();
        ScoutRank rank = ScoutRank.fromPoints(points);
        long predictions = stats == null ? 0 : stats.getPredictionsCount();
        long successful = stats == null ? 0 : stats.getSuccessfulPredictions();
        long failed = stats == null ? 0 : stats.getFailedPredictions();
        long expired = stats == null ? 0 : stats.getExpiredPredictions();
        Double accuracy = stats == null ? null : stats.accuracy();
        int currentStreak = stats == null ? 0 : stats.getCurrentSuccessStreak();
        int bestStreak = stats == null ? 0 : stats.getBestSuccessStreak();
        ScoutStatsResponse scout = stats == null ? null : ScoutStatsResponse.from(stats);
        List<AchievementResponse> achievementResponses = achievements.stream()
                .map(a -> {
                    var def = AchievementService.CATALOG.get(a.getAchievementCode());
                    return new AchievementResponse(
                            a.getAchievementCode(),
                            def == null ? a.getAchievementCode() : def.title(),
                            def == null ? "" : def.description(),
                            a.getUnlockedAt()
                    );
                })
                .toList();
        return new UserProfileResponse(
                u.getId(), u.getNickname(), u.getVotesCount(), u.getSubmittedMemesCount(), u.status(),
                points, rank, predictions, successful, failed, expired, accuracy, currentStreak, bestStreak,
                scout, achievementResponses, u.getCreatedAt()
        );
    }
}
