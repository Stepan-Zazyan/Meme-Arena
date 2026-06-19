package ru.memearena.vote.api; import java.util.UUID; public record VoteRatingChangeResponse(UUID memeId,int ratingBefore,int ratingAfter){}
