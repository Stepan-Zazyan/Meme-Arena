# Мемный скаут

Ранний прогноз создаётся backend-ом при обычном голосовании за победителя, если мем APPROVED, одобрен не больше 24 часов назад, до голоса имел меньше 30 битв, а пользователь ещё не прогнозировал этот мем.

Cohort — все APPROVED-мемы одного календарного UTC-дня. После задержки resolution и при достаточном числе битв resolver сравнивает мемы cohort по Wilson lower bound; топ 25% считается успешным, минимум один мем проходит. Если подходящих мемов меньше четырёх, используется fallback: минимум битв, win rate 60% и Elo 1532.

Очки начисляются только при SUCCESS: 50/30/15 за поддержку до 5/15/30-й битвы и бонус +20 в первый час или +10 в первые 6 часов после approve. FAILED даёт 0 и сбрасывает серию, EXPIRED даёт 0 и не влияет на accuracy/series.

Accuracy = success / (success + failed). EXPIRED не учитывается. Ранги вычисляются из scout points: OBSERVER, SCOUT, TREND_HUNTER, MEME_ORACLE.

Resolver идемпотентен за счёт перехода только PENDING-прогнозов и unique constraints. В MVP scheduled resolver рассчитан на один backend instance; при горизонтальном масштабировании нужен distributed scheduling lock.
