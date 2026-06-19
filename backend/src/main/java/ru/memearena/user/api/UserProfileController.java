package ru.memearena.user.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.memearena.scout.api.ScoutDtos.AchievementResponse;
import ru.memearena.scout.api.ScoutDtos.ScoutStatsResponse;
import ru.memearena.scout.application.AchievementService;
import ru.memearena.scout.application.ScoutService;
import ru.memearena.security.CurrentUser;
import ru.memearena.user.application.UserProfileService;
import ru.memearena.user.domain.UserProfile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {
    private final UserProfileService service;
    private final ScoutService scout;
    private final AchievementService achievements;

    public UserProfileController(UserProfileService service, ScoutService scout, AchievementService achievements) {
        this.service = service;
        this.scout = scout;
        this.achievements = achievements;
    }

    @PostMapping("/guest")
    @Operation(summary = "Create guest user")
    public ResponseEntity<CreateGuestUserResponse> create(@Valid @RequestBody CreateGuestUserRequest request) {
        var created = service.createGuest(request.nickname(), request.installationId(), null);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateGuestUserResponse(toResponse(created.user()), created.accessToken(), created.expiresAt()));
    }

    @GetMapping("/me")
    public UserProfileResponse me() {
        return toResponse(service.get(CurrentUser.required().userId()));
    }

    @PostMapping("/me/logout")
    public ResponseEntity<Void> logout() {
        service.logout(CurrentUser.required().sessionId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get public user profile")
    public UserProfileResponse get(@PathVariable UUID userId) {
        return toResponse(service.get(userId));
    }

    private UserProfileResponse toResponse(UserProfile user) {
        Instant now = Instant.now();
        ScoutStatsResponse scoutStats = ScoutStatsResponse.from(scout.stats(user.getId(), now));
        List<AchievementResponse> achievementResponses = achievements.byUser(user.getId()).stream()
            .map(a -> {
                var def = AchievementService.CATALOG.get(a.getAchievementCode());
                String title = def == null ? a.getAchievementCode() : def.title();
                String description = def == null ? "" : def.description();
                return new AchievementResponse(a.getAchievementCode(), title, description, a.getUnlockedAt());
            })
            .toList();
        return UserProfileResponse.from(user, scoutStats, achievementResponses);
    }
}
