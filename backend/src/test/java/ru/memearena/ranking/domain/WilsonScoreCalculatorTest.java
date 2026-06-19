package ru.memearena.ranking.domain;
import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class WilsonScoreCalculatorTest { @Test void oneWinDoesNotBeatManyStableWins(){ assertThat(WilsonScoreCalculator.lowerBound(1,0)).isLessThan(WilsonScoreCalculator.lowerBound(80,20)); } }
