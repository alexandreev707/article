# Запуск проекта на Render

## Blueprint Path

В настройках Blueprint в Render укажите:

| Поле | Значение |
|------|----------|
| **Blueprint Path** | `render.yaml` |

Файл лежит **в корне репозитория**. Если перенесёте в подпапку (например `infra/render.yaml`), укажите этот путь.

---

## Шаги (через Blueprint)

1. **GitHub / GitLab**  
   Закоммитьте и запушьте репозиторий (включая `render.yaml` и `application-prod.yml`).

2. **Render Dashboard**  
   - **New +** → **Blueprint**  
   - Подключите репозиторий  
   - **Blueprint Path:** `render.yaml` (по умолчанию Render ищет `render.yaml` в корне)

3. **Применить**  
   Render создаст:
   - **PostgreSQL** (`marketplace-db`)
   - **Web Service** (`marketplace-mvp`) с переменными `DB_*` из БД

4. **Дождаться деплоя**  
   Сборка: `./gradlew clean build -x test`  
   Старт: JAR `marketplace-mvp-1.0.0.jar` на порту из `$PORT`

5. **Открыть URL**  
   После успешного деплоя откройте выданный адрес вида `https://marketplace-mvp.onrender.com`

---

## Если не используете Blueprint (вручную)

1. **New PostgreSQL** — запомните Internal Database URL / host, user, password, database.
2. **New Web Service** — тот же репозиторий:
   - **Runtime:** Java  
   - **Build:** `chmod +x ./gradlew && ./gradlew clean build -x test --no-daemon`  
   - **Start:** `java -Dserver.port=$PORT -jar build/libs/marketplace-mvp-1.0.0.jar`
3. **Environment variables:**
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `DB_HOST`, `DB_PORT` (обычно `5432`), `DB_NAME`, `DB_USER`, `DB_PASSWORD` — как у вашей БД на Render
4. В настройках сервиса привяжите БД (или пропишите переменные вручную).

---

## Важно

- **Первый деплой:** Flyway применит миграции к пустой БД.
- **Секреты:** не коммитьте пароли; на Render они задаются через Environment.
- **Бесплатный план:** сервис «засыпает» после простоя; первый запрос может быть долгим.
- **Health check:** в `render.yaml` указан путь `/actuator/health` — Spring Actuator должен быть в зависимостях (у вас есть).

---

## Возможные проблемы

| Проблема | Что проверить |
|----------|----------------|
| Build failed | Логи Build — версия Java 17, права на `./gradlew` |
| DB connection refused | `DB_HOST` — для Web Service нужен **Internal** host БД на Render |
| 502 после старта | Логи Runtime — ошибки Flyway или порта |
| Неверное имя JAR | После `./gradlew build` имя в `build/libs/` = `marketplace-mvp-1.0.0.jar` (см. `settings.gradle.kts` + `version` в `build.gradle.kts`) |
