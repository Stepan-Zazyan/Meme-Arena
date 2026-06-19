package ru.memearena.user.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.memearena.scout.application.AchievementService;
import ru.memearena.scout.application.ScoutService;
import ru.memearena.security.AuthenticatedUserPrincipal;
import ru.memearena.user.application.UserProfileService;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {
    private final UserProfileService service;
    private final ScoutService scout;
    private final AchievementService achievements;
    private final Clock clock;

    public UserProfileController(UserProfileService service, ScoutService scout, AchievementService achievements, Clock clock) {
        this.service = service;
        this.scout = scout;
        this.achievements = achievements;
        this.clock = clock;
    }

    @PostMapping("/guest")
    @Operation(summary = "Create guest user")
    public ResponseEntity<UserProfileResponse> create(@Valid @RequestBody CreateGuestUserRequest r) {
        var user = service.createGuest(r.nickname()).user();
        return ResponseEntity.status(HttpStatus.CREATED).body(profile(user.getId()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public UserProfileResponse me(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @RequestParam(required = false) UUID userId) {
        UUID id = principal != null ? principal.userId() : userId;
        return profile(id);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get public user profile")
    public UserProfileResponse get(@PathVariable UUID userId) {
        return profile(userId);
    }

    private UserProfileResponse profile(UUID userId) {
        var user = service.get(userId);
        var stats = scout.stats(userId, Instant.now(clock));
        return UserProfileResponse.from(user, stats, achievements.byUser(userId));
    }
}
