#!/bin/bash

echo "🚀 Запуск CryptoDrop Marketplace MVP"
echo ""

# Проверка Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен. Установите Docker для запуска инфраструктуры."
    exit 1
fi

# Запуск инфраструктуры
echo "📦 Запуск инфраструктуры (PostgreSQL, Kafka)..."
docker-compose up -d

echo "⏳ Ожидание готовности PostgreSQL..."
sleep 5

# Проверка PostgreSQL
until docker exec cryptodrop-postgres pg_isready -U postgres &> /dev/null; do
    echo "Ожидание PostgreSQL..."
    sleep 2
done

echo "✅ PostgreSQL готов"
echo ""

# Запуск Backend
echo "🔧 Запуск Backend (Spring Boot)..."
echo "Откройте новый терминал и выполните:"
echo "  ./gradlew bootRun"
echo ""

# Запуск Frontend
echo "🎨 Запуск Frontend (React)..."
echo "Откройте новый терминал и выполните:"
echo "  cd frontend && npm install && npm run dev"
echo ""

echo "✅ Инфраструктура запущена!"
echo ""
echo "📝 Следующие шаги:"
echo "1. Backend будет доступен на http://localhost:8080/api"
echo "2. Frontend будет доступен на http://localhost:3000"
echo "3. Проверьте статус: docker-compose ps"
