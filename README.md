# Meme Arena

## Запуск в IntelliJ IDEA

1. Откройте корневой каталог репозитория как проект.
2. Дождитесь импорта Maven-проекта `backend/pom.xml` и убедитесь, что Project SDK установлен в Java 21.
3. В списке Run Configurations выберите **Meme Arena PostgreSQL** и запустите её. Конфигурация поднимает только PostgreSQL 17 на локальном порту `5433` и хранит данные в Docker volume.
4. После готовности PostgreSQL выберите **Meme Arena Backend** и запустите её. Конфигурация включает профиль `local`; backend будет доступен на `http://localhost:8080`.

Значения в shared-конфигурациях предназначены только для локальной разработки. Их можно переопределить переменными окружения; реальные секреты в репозиторий добавлять нельзя.

## Тесты в IntelliJ IDEA

Запустите shared Run Configuration **Meme Arena Tests**. Тесты самостоятельно поднимают PostgreSQL 17 через Testcontainers, применяют Flyway-миграции и не используют локальную базу. Для запуска необходим работающий Docker.

## Проверка через Postman

1. Импортируйте `postman/Meme Arena.postman_collection.json`.
2. Импортируйте `postman/Local.postman_environment.json` и выберите окружение **Local**.
3. Запустите PostgreSQL и backend через shared Run Configurations.
4. Выполните запрос **Health** в коллекции. Postman проверит HTTP 200, поля ответа и заголовок `X-Request-Id`.

Swagger UI доступен по `/swagger-ui.html`, OpenAPI JSON — по `/v3/api-docs`, Actuator health и info — по `/actuator/health` и `/actuator/info`.

## Структура

- `backend/` — Spring Boot 3.5 backend на Java 21, конфигурации, Flyway и интеграционные тесты.
- `.run/` — shared Run Configurations для PostgreSQL, backend и тестов.
- `postman/` — локальное окружение и health-коллекция.
- `docker-compose.yml` — только PostgreSQL 17 для локальной разработки.
- `.env.example` — перечень поддерживаемых переменных окружения с безопасными локальными значениями.

На текущем этапе реализован только backend foundation. Мемы, пользователи, голосование, рейтинги, загрузка файлов, мобильное приложение и прочая бизнес-функциональность отсутствуют.
