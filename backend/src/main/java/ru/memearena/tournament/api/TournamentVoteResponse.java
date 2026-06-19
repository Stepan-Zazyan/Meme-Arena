package ru.memearena.tournament.api; import java.util.*; public record TournamentVoteResponse(UUID matchId,UUID selectedMemeId,long leftVotes,long rightVotes){}
