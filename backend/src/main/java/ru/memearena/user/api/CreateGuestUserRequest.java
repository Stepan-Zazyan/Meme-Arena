package ru.memearena.user.api;
import jakarta.validation.constraints.NotBlank; public record CreateGuestUserRequest(@NotBlank String nickname) {}
