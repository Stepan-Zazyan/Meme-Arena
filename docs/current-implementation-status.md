# Current implementation status

Аудит сделан по текущему рабочему дереву Meme Arena: backend `backend/src`, Flutter `mobile/lib`, миграции Flyway, Postman, Docker Compose и README. Maven-проверка в этом окружении блокируется прокси Maven Central (HTTP 403 при загрузке Spring Boot parent), поэтому автоматические пункты, зависящие от Maven dependency resolution, отмечены как непроверенные окружением.

| Компонент | Backend реализован | Mobile реализован | Tests существуют | Реально проверено | Проблемы | Действие на этом этапе |
|---|---:|---:|---:|---|---|---|
| Foundation | Да: Spring Boot, request id, error handler, profiles local/test | Да: базовый Flutter app | Да | Проверены файлы, conflict markers через `rg` | Prod profile отсутствовал | Добавлен prod profile, CI |
| Auth | Да: guest, bearer sessions, logout | Да: secure session store | Да | Код просмотрен | Maven baseline недоступен из-за 403 | Сохранено, добавлена аналитика с principal |
| Users | Да | Да | Да | Код просмотрен | Нет явных дублей DTO | Без крупных изменений |
| Media | Да: MinIO/in-memory, multipart, content endpoint | Да: upload flow | Да | Код просмотрен | Требует runtime-проверки с Maven/Docker | Без крупных изменений |
| Memes | Да | Да | Да | Код просмотрен | Требует smoke-теста | Без крупных изменений |
| Moderation | Да: admin/local controllers | Частично: пользовательский upload, нет admin UI | Да | Код просмотрен | Admin token только backend/Postman | Без крупных изменений |
| Battles | Да | Да | Да | Код просмотрен | Требует runtime-теста | Без крупных изменений |
| Votes | Да: vote + Elo + prediction | Да | Да | Код просмотрен | Требует runtime-теста | Без крупных изменений |
| Meme leaderboard | Да | Да | Да | Код просмотрен | Нет | Без крупных изменений |
| Scout core | Да | Нет отдельной core-модели вне UI | Да | Код просмотрен | Требует resolver runtime-теста | Без крупных изменений |
| Scout API | Да | Да | Да | Код просмотрен | Нет | Без крупных изменений |
| Scout mobile UI | Backend N/A | Да | Да | Код просмотрен | Требует Flutter analyze/test | Добавлен analytics hook best effort |
| Tournament backend | Да: текущий HEAD содержит tournament domain/api/services | Да API доступен | Частично | Код просмотрен | Не проверен runtime | Не реализовывался заново |
| Tournament mobile UI | Backend N/A | Да, если endpoints доступны | Частично | Код просмотрен | Не проверен runtime | Сохранено |
| Postman | N/A | N/A | Collection есть | Файл просмотрен | Не было RC Smoke Flow | Добавлен RC Smoke Flow |
| OpenAPI | springdoc + scout yaml | N/A | Нет | Файлы просмотрены | Нет полного статического контракта | README/Postman синхронизированы частично |
| Flyway | Да | N/A | Через IT | Версии просмотрены | Нет V4, есть local V7 отдельно; Maven/Flyway runtime недоступен | Добавлена V8 product_event |
| Docker Compose | Да: PostgreSQL/MinIO | N/A | Нет | Файл просмотрен | Runtime не подтвержден | Сохранено |
| IntelliJ Run Configurations | Да | Да | N/A | `.run` просмотрены | Нет | Сохранено |
| Android release configuration | N/A | Частично | Flutter tests | Код просмотрен | Требует проверки APK; release URL/secrets требуют донастройки | Настроены applicationId/name/build config README |

## Проверки конфликтов и дублей

- Conflict markers standard Git conflict-marker triplet: не найдены в отслеживаемом исходном коде командой `rg`.
- Миграции с одинаковым номером: основная последовательность содержит V1, V2, V3, V5, V6, V7, V8; локальная seed migration имеет `db/migration/local/V7`, что применяется только local/test locations.
- Полностью отсутствующий Tournament не подтверждён: в HEAD он частично/фактически реализован, поэтому не создавался заново.
- Дублирующиеся DTO/enum/endpoint по статическому осмотру не выявлены; автоматическая компиляционная проверка заблокирована Maven dependency resolution в окружении.
