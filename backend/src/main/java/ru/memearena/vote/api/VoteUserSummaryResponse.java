package ru.memearena.vote.api; import ru.memearena.scout.domain.ScoutRank; public record VoteUserSummaryResponse(long votesCount,long scoutPoints,ScoutRank rank){}
