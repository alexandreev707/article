# CryptoDrop Marketplace MVP

Децентрализованный маркетплейс физических товаров с оплатой криптовалютой (USDT/USDC) на блокчейне Solana.

## 🚀 Быстрый старт

### Требования

- Java 17+
- Node.js 18+
- Docker и Docker Compose
- PostgreSQL 15+ (или используйте Docker)
- Kafka (или используйте Docker)

### Windows: Автоматический запуск

```bash
# Запустить инфраструктуру и получить инструкции
start.bat
```

### Linux/Mac: Автоматический запуск

```bash
# Сделать скрипт исполняемым
chmod +x start.sh

# Запустить инфраструктуру
./start.sh
```

### Ручной запуск

#### 1. Запуск инфраструктуры

```bash
# Запустить PostgreSQL и Kafka через Docker Compose
docker-compose up -d

# Проверить статус
docker-compose ps

# Проверить логи
docker-compose logs -f
```

### 2. Настройка переменных окружения

Создайте файл `.env` в корне проекта (опционально, можно использовать application.yml):

```env
SOLANA_ESCROW_PROGRAM_ID=your_program_id
SOLANA_PLATFORM_WALLET=your_wallet_address
DHL_API_KEY=your_dhl_key
DHL_SITE_ID=your_site_id
DHL_PASSWORD=your_password
```

### 3. Запуск Backend (Spring Boot)

**Windows:**
```bash
# Сборка проекта
gradlew.bat build

# Запуск приложения
gradlew.bat bootRun
```

**Linux/Mac:**
```bash
# Сделать gradlew исполняемым (первый раз)
chmod +x gradlew

# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun
```

**Или через IDE:**
- Откройте проект в IntelliJ IDEA / Eclipse
- Запустите `CryptoDropApplication.kt`

Backend будет доступен на `http://localhost:8080/api`

**Проверка работы:**
```bash
# Проверить health endpoint
curl http://localhost:8080/api/actuator/health

# Получить список категорий
curl http://localhost:8080/api/v1/categories
```

### 4. Запуск Frontend (React)

```bash
cd frontend

# Установка зависимостей (первый раз)
npm install

# Запуск dev сервера
npm run dev
```

Frontend будет доступен на `http://localhost:3000`

**Проверка работы:**
- Откройте браузер: `http://localhost:3000`
- Должна загрузиться главная страница с каталогом
- Нажмите "Подключить кошелек" для подключения Phantom/Solflare

## 📋 API Endpoints

### Products
- `GET /api/v1/products` - Список товаров
- `GET /api/v1/products/{id}` - Детали товара
- `POST /api/v1/products` - Создать товар (требует X-Wallet-Address header)
- `GET /api/v1/products/seller/{wallet}` - Товары продавца

### Categories
- `GET /api/v1/categories` - Все категории
- `GET /api/v1/categories/roots` - Корневые категории
- `GET /api/v1/categories/{slug}` - Категория по slug

### Orders
- `POST /api/v1/orders` - Создать заказ (требует X-Wallet-Address header)
- `GET /api/v1/orders/{id}` - Детали заказа
- `GET /api/v1/orders/buyer/my-orders` - Заказы покупателя
- `GET /api/v1/orders/seller/my-orders` - Заказы продавца
- `POST /api/v1/orders/{id}/confirm-payment` - Подтвердить оплату
- `POST /api/v1/orders/{id}/confirm-delivery` - Подтвердить доставку

## 🧪 Тестирование

### Backend тесты

```bash
# Запуск всех тестов
./gradlew test

# Запуск с покрытием
./gradlew test jacocoTestReport
```

### API тестирование (Postman/cURL)

```bash
# Получить список товаров
curl http://localhost:8080/api/v1/products

# Получить категории
curl http://localhost:8080/api/v1/categories

# Создать товар (требует кошелек)
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -H "X-Wallet-Address: YourWalletAddress" \
  -d '{
    "categoryId": 1,
    "title": "Test Product",
    "description": "Test Description",
    "priceUsd": 99.99,
    "images": ["https://example.com/image.jpg"]
  }'
```

### Frontend тестирование

```bash
cd frontend

# Запуск тестов (если добавлены)
npm test

# Проверка типов
npm run type-check
```

## 🏗️ Структура проекта

```
.
├── src/main/kotlin/com/cryptodrop/
│   ├── api/              # REST контроллеры и DTO
│   ├── config/           # Конфигурация Spring
│   ├── domain/           # Модели и репозитории
│   ├── integration/      # Интеграции (Solana, DHL)
│   └── service/          # Бизнес-логика
├── src/main/resources/
│   ├── db/migration/     # Flyway миграции
│   └── application.yml   # Конфигурация приложения
├── frontend/
│   ├── src/
│   │   ├── components/   # React компоненты
│   │   ├── pages/        # Страницы
│   │   ├── services/     # API клиент
│   │   └── contexts/     # React контексты
│   └── package.json
├── docker-compose.yml    # Инфраструктура
└── build.gradle.kts      # Gradle конфигурация
```

## 🔧 Разработка

### Добавление новой миграции БД

```bash
# Создайте файл в src/main/resources/db/migration/
# Название: V{номер}__{описание}.sql
# Например: V3__add_user_ratings.sql
```

### Добавление нового API endpoint

1. Создайте DTO в `api/dto/`
2. Добавьте метод в соответствующий Service
3. Создайте endpoint в Controller
4. Обновите документацию

### Работа с Solana

Для разработки используйте Solana Devnet:

```yaml
# application.yml
solana:
  rpc-url: https://api.devnet.solana.com
  network: devnet
```

## 📦 Сборка для production

### Backend

```bash
./gradlew clean build
# JAR файл будет в build/libs/cryptodrop-marketplace-1.0.0-SNAPSHOT.jar

# Запуск
java -jar build/libs/cryptodrop-marketplace-1.0.0-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
# Статические файлы в frontend/dist/
```

## 🐛 Troubleshooting

### PostgreSQL не запускается

```bash
# Проверьте логи
docker-compose logs postgres

# Пересоздайте контейнер
docker-compose down -v
docker-compose up -d postgres
```

### Kafka не работает

```bash
# Проверьте логи
docker-compose logs kafka

# Убедитесь что Zookeeper запущен
docker-compose ps
```

### Порт 8080 занят

Измените порт в `application.yml`:

```yaml
server:
  port: 8081
```

### Frontend не подключается к API

Проверьте `vite.config.ts` - proxy должен указывать на правильный порт backend.

## 📝 TODO для MVP

- [x] Базовая структура проекта
- [x] Модели данных (Product, Order, Category)
- [x] REST API endpoints
- [x] Frontend компоненты
- [ ] Реальная интеграция с Solana (сейчас mock)
- [ ] Интеграция с DHL API
- [ ] Wallet signature verification
- [ ] Тесты (unit + integration)
- [ ] Документация API (Swagger)

## 📄 Лицензия

MIT
