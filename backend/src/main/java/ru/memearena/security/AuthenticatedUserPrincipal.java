package ru.memearena.security; import java.util.UUID; public record AuthenticatedUserPrincipal(UUID userId,String nickname,UUID sessionId) {}
