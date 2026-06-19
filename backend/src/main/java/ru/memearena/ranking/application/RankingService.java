package ru.memearena.ranking.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.memearena.meme.domain.Meme;
import ru.memearena.meme.infrastructure.MemeRepository;
import ru.memearena.ranking.api.*;
import ru.memearena.ranking.domain.WilsonScoreCalculator;
import ru.memearena.vote.domain.Vote;
import ru.memearena.vote.infrastructure.VoteRepository;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingService {
    private final MemeRepository memes;
    private final VoteRepository votes;
    private final Clock clock;
    public RankingService(MemeRepository memes, VoteRepository votes, Clock clock){this.memes=memes;this.votes=votes;this.clock=clock;}
    @Transactional(readOnly=true)
    public TopMemesResponse top(RankingPeriod period, int limit){
        return period==RankingPeriod.ALL_TIME ? allTime(limit) : period(period, limit);
    }
    private TopMemesResponse allTime(int limit){
        var list=memes.topAllTime(PageRequest.of(0,limit));
        List<TopMemeItemResponse> items=new ArrayList<>(); int pos=1;
        for(Meme m:list) items.add(new TopMemeItemResponse(pos++,m.getId(),m.getTitle(),m.getImageUrl(),m.getMediaAssetId(),TopMemeItemResponse.contentUrl(m),m.getRating(),m.getWins(),m.getLosses(),m.getBattlesCount(),m.getWins(),m.getLosses(),m.getBattlesCount(),m.getBattlesCount()==0?0.0:(double)m.getWins()/m.getBattlesCount(),m.getRating()));
        return new TopMemesResponse(RankingPeriod.ALL_TIME,items);
    }
    private TopMemesResponse period(RankingPeriod period,int limit){
        Instant from=Instant.now(clock).minus(period==RankingPeriod.DAY?Duration.ofHours(24):Duration.ofDays(7));
        Map<UUID,long[]> stats=new HashMap<>();
        for(Vote v:votes.findSince(from)){ stats.computeIfAbsent(v.getWinnerMemeId(),k->new long[2])[0]++; stats.computeIfAbsent(v.getLoserMemeId(),k->new long[2])[1]++; }
        var approved=memes.findByModerationStatus(ru.memearena.meme.domain.ModerationStatus.APPROVED, PageRequest.of(0,10000)).getContent();
        var ranked=approved.stream().map(m->{long[] s=stats.getOrDefault(m.getId(),new long[2]); long b=s[0]+s[1]; double score=WilsonScoreCalculator.lowerBound(s[0],s[1]); return new Row(m,s[0],s[1],b,b==0?0.0:(double)s[0]/b,score);})
                .sorted(Comparator.comparingDouble(Row::score).reversed().thenComparing(Comparator.comparingLong(Row::battles).reversed()).thenComparing(r->r.meme.getCreatedAt())).limit(limit).toList();
        List<TopMemeItemResponse> items=new ArrayList<>(); int pos=1; for(Row r:ranked){Meme m=r.meme; items.add(new TopMemeItemResponse(pos++,m.getId(),m.getTitle(),m.getImageUrl(),m.getMediaAssetId(),TopMemeItemResponse.contentUrl(m),m.getRating(),m.getWins(),m.getLosses(),m.getBattlesCount(),r.wins,r.losses,r.battles,r.rate,r.score));}
        return new TopMemesResponse(period,items);
    }
    private record Row(Meme meme,long wins,long losses,long battles,double rate,double score){}
}
