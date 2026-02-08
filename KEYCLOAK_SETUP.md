# Keycloak Setup Guide

## Быстрая настройка Keycloak для Marketplace MVP

### Шаг 1: Доступ к Keycloak Admin Console

1. Откройте браузер и перейдите на:
   ```
   http://localhost:8081
   ```

2. Войдите с учетными данными:
   - Username: `admin`
   - Password: `admin`

### Шаг 2: Создание Realm

1. В левом верхнем углу нажмите на выпадающий список (по умолчанию "Master")
2. Выберите "Create Realm"
3. Введите название: `marketplace`
4. Нажмите "Create"

### Шаг 3: Создание Client

1. В меню слева выберите "Clients"
2. Нажмите "Create client"
3. Заполните форму:
   - **Client ID**: `marketplace-client`
   - **Client authentication**: OFF (Public client)
   - Нажмите "Next"
4. На вкладке "Capability config":
   - Включите "Standard flow"
   - Включите "Direct access grants" (опционально, для тестирования)
   - Нажмите "Next"
5. На вкладке "Login settings":
   - **Valid redirect URIs**: `http://localhost:8080/*`
   - **Web origins**: `http://localhost:8080`
   - Нажмите "Save"

### Шаг 4: Настройка Google Identity Provider (опционально)

1. В меню слева выберите "Identity providers"
2. Нажмите "Add provider" → "Google"
3. Заполните:
   - **Client ID**: (ваш Google OAuth2 Client ID)
   - **Client Secret**: (ваш Google OAuth2 Client Secret)
4. Нажмите "Add"
5. Включите "Trust Email"

**Как получить Google OAuth2 credentials:**
1. Перейдите на https://console.cloud.google.com/
2. Создайте новый проект или выберите существующий
3. Перейдите в "APIs & Services" → "Credentials"
4. Нажмите "Create Credentials" → "OAuth client ID"
5. Выберите "Web application"
6. Добавьте Authorized redirect URIs: `http://localhost:8081/realms/marketplace/broker/google/endpoint`
7. Скопируйте Client ID и Client Secret

### Шаг 5: Создание ролей

1. В меню слева выберите "Realm roles"
2. Нажмите "Create role" и создайте следующие роли:
   - `ROLE_CUSTOMER`
   - `ROLE_SELLER`
   - `ROLE_ADMIN`

### Шаг 6: Создание пользователя-администратора

1. В меню слева выберите "Users"
2. Нажмите "Create new user"
3. Заполните:
   - **Username**: `admin`
   - **Email**: `admin@marketplace.com`
   - **Email verified**: ON
   - **First name**: `Admin`
   - **Last name**: `User`
4. Нажмите "Create"

5. Перейдите на вкладку "Credentials"
6. Установите пароль:
   - **Password**: (введите пароль)
   - **Password confirmation**: (повторите пароль)
   - **Temporary**: OFF
7. Нажмите "Save"

8. Перейдите на вкладку "Role mapping"
9. Нажмите "Assign role"
10. Выберите фильтр "Filter by realm roles"
11. Выберите `ROLE_ADMIN`
12. Нажмите "Assign"

### Шаг 7: Настройка Client Roles (для автоматического маппинга)

1. Перейдите в "Clients" → `marketplace-client`
2. Перейдите на вкладку "Roles"
3. Создайте роли (если их нет):
   - `CUSTOMER`
   - `SELLER`
   - `ADMIN`

4. Перейдите на вкладку "Mappers"
5. Нажмите "Create mapper" → "By configuration" → "User Realm Role"
6. Заполните:
   - **Name**: `realm-roles-mapper`
   - **Token Claim Name**: `realm_access.roles`
   - **Claim JSON Type**: `String`
   - **Add to ID token**: ON
   - **Add to access token**: ON
   - **Add to userinfo**: ON
7. Нажмите "Save"

### Шаг 8: Проверка настроек

1. Убедитесь, что realm `marketplace` активен
2. Проверьте, что client `marketplace-client` настроен правильно
3. Проверьте, что пользователь `admin` создан и имеет роль `ROLE_ADMIN`
4. Попробуйте войти в приложение через Keycloak

### Troubleshooting

**Проблема**: Не могу войти в Keycloak
- Решение: Проверьте, что контейнер Keycloak запущен: `docker-compose ps`
- Проверьте логи: `docker-compose logs keycloak`

**Проблема**: Ошибка "Invalid redirect URI"
- Решение: Убедитесь, что в настройках client добавлен `http://localhost:8080/*` в Valid redirect URIs

**Проблема**: Роли не применяются
- Решение: Проверьте настройки маппера ролей и убедитесь, что роли назначены пользователю

**Проблема**: Google OAuth не работает
- Решение: Проверьте, что redirect URI в Google Console совпадает с настройками Keycloak

### Полезные ссылки

- Keycloak Documentation: https://www.keycloak.org/documentation
- Keycloak Admin REST API: https://www.keycloak.org/docs-api/latest/rest-api/




