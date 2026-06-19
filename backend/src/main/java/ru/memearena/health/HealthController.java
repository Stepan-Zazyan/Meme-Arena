package ru.memearena.health;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    private final Clock clock;
    private final String version;

    public HealthController(Clock clock, @Value("${info.app.version}") String version) {
        this.clock = clock;
        this.version = version;
    }

    @Operation(summary = "Check backend availability")
    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("UP", "meme-arena-backend", version, Instant.now(clock));
    }
}
