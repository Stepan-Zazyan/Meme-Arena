package ru.memearena.user.api;
import jakarta.validation.constraints.*; public record CreateGuestUserRequest(@NotBlank String nickname,@Size(min=10,max=100) String installationId) {}
