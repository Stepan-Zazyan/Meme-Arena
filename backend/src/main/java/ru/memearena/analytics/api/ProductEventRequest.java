package ru.memearena.analytics.api;
import jakarta.validation.constraints.*; import java.time.Instant;
public record ProductEventRequest(@NotBlank String eventType, @NotNull Instant occurredAt, @Size(max=30) String appVersion, @Size(max=20) String platform) {}
