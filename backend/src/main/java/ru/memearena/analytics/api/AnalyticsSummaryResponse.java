package ru.memearena.analytics.api;
public record AnalyticsSummaryResponse(long newUsers,long activeUsers,long appOpens,long completedVotes,long uploadedMemes,long createdPredictions,long tournamentVotes,long usersWhoOpenedBattle,long usersWhoCompletedVote,double battleToVoteConversion) {}
