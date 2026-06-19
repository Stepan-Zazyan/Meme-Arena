# Достижения

Каталог фиксирован в backend: FIRST_VOTE, FIRST_PREDICTION, FIRST_HIT, EARLY_BIRD, STREAK_3, STREAK_10, SCOUT_RANK, TREND_HUNTER_RANK, MEME_ORACLE_RANK, VOTER_100.

Разблокировка происходит в backend при создании прогнозов и разрешении прогнозов; клиент только отображает полученный список. Таблица user_achievement имеет unique `(user_id, achievement_code)`, поэтому повторная разблокировка идемпотентна. Metadata не содержит критичную бизнес-логику.
