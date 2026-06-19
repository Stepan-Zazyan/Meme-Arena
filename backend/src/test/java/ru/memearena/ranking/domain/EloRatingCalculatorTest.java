package ru.memearena.ranking.domain;
import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class EloRatingCalculatorTest { @Test void equalRatingsMoveBySixteen(){ var r=EloRatingCalculator.calculate(1500,1500); assertThat(r.winnerRatingAfter()).isEqualTo(1516); assertThat(r.loserRatingAfter()).isEqualTo(1484);} }
