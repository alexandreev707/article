@echo off
echo 🚀 Запуск CryptoDrop Marketplace MVP
echo.

REM Проверка Docker
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker не установлен. Установите Docker для запуска инфраструктуры.
    pause
    exit /b 1
)

REM Запуск инфраструктуры
echo 📦 Запуск инфраструктуры (PostgreSQL, Kafka)...
docker-compose up -d

timeout /t 5 /nobreak >nul

echo ✅ Инфраструктура запущена!
echo.
echo 📝 Следующие шаги:
echo 1. Запустите Backend: gradlew.bat bootRun
echo 2. В другом терминале запустите Frontend: cd frontend ^&^& npm install ^&^& npm run dev
echo 3. Backend будет доступен на http://localhost:8080/api
echo 4. Frontend будет доступен на http://localhost:3000
echo.
pause
