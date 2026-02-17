# Marketplace MVP

Полнофункциональный MVP маркетплейса физических товаров с поддержкой покупателей, продавцов и администраторов.

## Технологический стек

- **Backend**: Kotlin + Spring Boot 3 (Web, Security, Data MongoDB)
- **Frontend**: JavaScript (ES6+), HTML5, CSS3 (Tailwind CSS) + Thymeleaf
- **База данных**: MongoDB
- **Аутентификация**: Keycloak (OAuth2/OIDC)
- **WebSocket**: SockJS + STOMP для in-memory чата
- **Контейнеризация**: Docker + Docker Compose

## Основной функционал

### Роли пользователей

- **Покупатель (ROLE_CUSTOMER)**: просмотр каталога, фильтрация, сортировка, добавление в избранное, покупка, чат с продавцом
- **Продавец (ROLE_SELLER)**: CRUD товаров, управление заказами, общение с покупателями
- **Администратор (ROLE_ADMIN)**: управление пользователями, блокировка, выдача прав, модерация контента

### Основные возможности

- ✅ Полноценный CRUD для товаров (с фото, миниатюрами, описанием, категориями)
- ✅ Система рейтингов и отзывов к товарам
- ✅ Избранное для пользователей
- ✅ Главная страница с персонализированными рекомендациями по категориям
- ✅ Фильтрация и сортировка (по цене, рейтингу, дате, отзывам)
- ✅ Чат между покупателем и продавцом (in-memory WebSocket)
- ✅ Админ-панель для управления пользователями и контентом
- ✅ Аутентификация через Google OAuth2 (Keycloak)

## Быстрый старт

### Предварительные требования

- Docker и Docker Compose установлены
- JDK 21 (для локальной разработки)
- Gradle (для локальной сборки)

### Запуск через Docker Compose

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd marketplace-mvp
```

2. Запустите все сервисы:
```bash
docker-compose up -d
```

Это запустит:
- **MongoDB** на порту 27017
- **Keycloak** на порту 8081
- **Приложение** на порту 8080

3. Дождитесь полной загрузки всех сервисов (может занять 1-2 минуты)

4. Откройте браузер и перейдите на:
```
http://localhost:8080
```

### Настройка Keycloak

После первого запуска необходимо настроить Keycloak:

1. Откройте Keycloak Admin Console:
```
http://localhost:8081
```

2. Войдите с учетными данными:
   - Username: `admin`
   - Password: `admin`

3. Создайте Realm:
   - Нажмите на выпадающий список в левом верхнем углу (Master)
   - Выберите "Create Realm"
   - Название: `marketplace`
   - Нажмите "Create"

4. Создайте Client:
   - Перейдите в "Clients" → "Create client"
   - Client ID: `marketplace-client`
   - Client authentication: OFF (Public client)
   - Valid redirect URIs: `http://localhost:8080/*`
   - Web origins: `http://localhost:8080`
   - Нажмите "Save"

5. Настройте Google Identity Provider:
   - Перейдите в "Identity providers" → "Add provider" → "Google"
   - Вам понадобится Google OAuth2 Client ID и Secret
   - Сохраните настройки

6. Создайте роли:
   - Перейдите в "Realm roles" → "Create role"
   - Создайте роли: `ROLE_CUSTOMER`, `ROLE_SELLER`, `ROLE_ADMIN`

7. Создайте пользователя-администратора:
   - Перейдите в "Users" → "Create new user"
   - Username: `admin`
   - Email: `admin@marketplace.com`
   - Email verified: ON
   - Нажмите "Save"
   - Перейдите на вкладку "Credentials"
   - Установите пароль
   - Перейдите на вкладку "Role mapping"
   - Назначьте роль `ROLE_ADMIN`

### Локальная разработка

1. Убедитесь, что MongoDB и Keycloak запущены:
```bash
docker-compose up -d mongodb keycloak
```

2. Соберите проект:
```bash
./gradlew build
```

3. Запустите приложение:
```bash
./gradlew bootRun
```

Приложение будет доступно на `http://localhost:8080`

## Структура проекта

```
marketplace-mvp/
├── src/main/kotlin/com/cryptodrop/
│   ├── config/           # Конфигурации Security, WebSocket, MongoDB
│   ├── controller/        # REST и MVC контроллеры
│   ├── model/            # Data-классы (MongoDB документы)
│   ├── repository/       # MongoDB репозитории
│   ├── service/          # Бизнес-логика
│   ├── dto/              # DTO для API
│   ├── security/         # Интеграция с Keycloak
│   └── WebSocket/        # Конфигурация и обработчики чата
├── src/main/resources/
│   ├── static/           # CSS (Tailwind), JS, изображения
│   ├── templates/        # Thymeleaf HTML шаблоны
│   └── application.yml   # Конфигурация приложения
├── docker-compose.yml     # Развертывание MongoDB + Keycloak
├── Dockerfile            # Сборка приложения
└── README.md
```

## API Endpoints

### Публичные

- `GET /` - Главная страница
- `GET /products` - Список товаров с фильтрацией
- `GET /products/{id}` - Детали товара
- `GET /api/products` - REST API для товаров
- `GET /api/products/recommended` - Рекомендованные товары
- `GET /api/products/categories` - Список категорий

### Требуют аутентификации

- `POST /api/products` - Создать товар (SELLER/ADMIN)
- `PUT /api/products/{id}` - Обновить товар (SELLER/ADMIN)
- `POST /api/orders` - Создать заказ (CUSTOMER/ADMIN)
- `GET /api/orders` - Мои заказы
- `POST /api/reviews` - Создать отзыв (CUSTOMER/ADMIN)
- `POST /api/favorites/{productId}` - Добавить в избранное
- `GET /api/favorites` - Список избранного
- `POST /api/chat` - Отправить сообщение
- `GET /api/chat/conversation` - История чата

### Администраторские

- `GET /admin` - Админ-панель
- `GET /admin/users` - Список пользователей
- `POST /api/admin/users/{id}/block` - Заблокировать пользователя
- `POST /api/admin/users/{id}/unblock` - Разблокировать пользователя
- `POST /api/admin/users/{id}/roles/{role}` - Выдать роль
- `DELETE /api/admin/users/{id}/roles/{role}` - Отозвать роль

## WebSocket

Чат работает через WebSocket на эндпоинте `/ws/chat`:

- Подключение: `SockJS('/ws/chat')`
- Отправка сообщений: `/app/chat.send`
- Подписка на сообщения: `/topic/chat/{userId}`

## Конфигурация

Основные настройки в `application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://mongodb:27017/marketplace
  
keycloak:
  realm: marketplace
  auth-server-url: http://keycloak:8080
  resource: marketplace-client
```

## Тестирование

Запуск тестов:
```bash
./gradlew test
```

## Логирование

Логи доступны в консоли и в файлах. Уровень логирования настраивается в `application.yml`.

## Health Checks

Приложение предоставляет health-check эндпоинты:
- `GET /actuator/health` - Статус приложения
- `GET /actuator/info` - Информация о приложении
- `GET /actuator/metrics` - Метрики

## Кэширование

Используется Caffeine для кэширования:
- Товары (10 минут)
- Пользователи (10 минут)
- Категории (10 минут)

## Troubleshooting

### Проблемы с Keycloak

Если Keycloak не запускается:
1. Проверьте логи: `docker-compose logs keycloak`
2. Убедитесь, что порт 8081 свободен
3. Попробуйте пересоздать контейнер: `docker-compose up -d --force-recreate keycloak`

### Проблемы с MongoDB

Если MongoDB не подключается:
1. Проверьте логи: `docker-compose logs mongodb`
2. Убедитесь, что порт 27017 свободен
3. Проверьте подключение: `docker-compose exec mongodb mongosh`

### Проблемы с аутентификацией

Если не работает вход:
1. Проверьте настройки Keycloak realm и client
2. Убедитесь, что redirect URIs настроены правильно
3. Проверьте логи приложения на наличие ошибок

## Разработка

### Добавление нового функционала

1. Создайте модель в `model/`
2. Создайте репозиторий в `repository/`
3. Создайте сервис в `service/`
4. Создайте контроллер в `controller/`
5. Добавьте DTO в `dto/` при необходимости

### Стиль кода

- Используйте Kotlin coding conventions
- Следуйте принципам Clean Architecture
- Добавляйте логирование важных операций
- Обрабатывайте исключения корректно

## Лицензия

Этот проект создан для образовательных целей.

## Поддержка

При возникновении проблем создайте issue в репозитории проекта.

docker run -d \
--name marketplace-postgres \
-e POSTGRES_DB=cryptodrop \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=admin \
-p 5434:5432 \
-v postgres_data:/var/lib/postgresql/data \
postgres:16-alpine





