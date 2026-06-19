package ru.memearena.tournament.api; import jakarta.validation.constraints.*; import java.util.*; public record TournamentVoteRequest(@NotNull UUID selectedMemeId){}
