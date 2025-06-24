# FlexDB — Динамический CRUD/DDL API для PostgreSQL

Проект предоставляет REST API для создания и управления таблицами и данными в PostgreSQL без ручного изменения схемы БД. Поддерживаются безопасные DDL- и CRUD-операции, валидация данных, пагинация и глобальная обработка ошибок.

---

##  Возможности

###  DDL (управление таблицами)
- Создание таблицы с любыми колонками (`POST /api/v1/dynamic-tables/schemas`)
- Получение схемы таблицы (`GET /api/v1/dynamic-tables/{tableName}`)
- Получение всех таблиц (`GET /api/v1/dynamic-tables`)

### CRUD (работа с данными)
- Создание записи (`POST /api/v1/dynamic-tables/data/{tableName}`)
- Получение записи по ID (`GET /api/v1/dynamic-tables/data/{tableName}/{id}`)
- Пагинированный список записей (`GET /api/v1/dynamic-tables/data/{tableName}?page=0&size=20`)
- Обновление записи по ID (`PUT /api/v1/dynamic-tables/data/{tableName}/{id}`)
- Удаление записи (`DELETE /api/v1/dynamic-tables/data/{tableName}/{id}`)


##  Стек технологий

-  Java 17+
-  Spring Boot 3+
-  Spring Data JPA + JdbcTemplate
-  PostgreSQL
-  Валидация + защита от SQL-инъекций
-  Глобальная обработка ошибок (через `@ControllerAdvice`)

  ##  Инструкции по сборке и запуску

### 1. Установите PostgreSQL и создайте базу данных:

```sql
CREATE DATABASE flexdb;

2. Настройте application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flexdb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
Рекомендуе использовать .env или переменные среды для DB_USERNAME и DB_PASSWORD.

3. Сборка и запуск:
./mvnw clean install
./mvnw spring-boot:run
Приложение будет доступно по адресу: http://localhost:8080


## Примеры запросов

### Пример создания таблицы
POST /api/v1/dynamic-tables/schemas
Content-Type: application/json

{
  "tableName": "users_info",
  "userFriendlyName": "Юзеры",
  "columns": [
    {
      "name": "full_name",
      "type": "TEXT",
      "isNullable": false
    },
    {
      "name": "address_name",
      "type": "TEXT",
      "isNullable": true
    },
    {
      "name": "age_user",
      "type": "INTEGER",
      "isNullable": false
    },
    {
      "name": "is_active",
      "type": "BOOLEAN",
      "isNullable": true
    }
  ]
}

### Получение схемы таблицы
GET /api/v1/dynamic-tables/users_info
Ответ:
{
  "tableName": "users_info",
  "userFriendlyName": "Юзеры",
  "columns": [
    {
      "name": "id",
      "type": "BIGINT",
      "postgresType": "BIGSERIAL",
      "nullable": false,
      "primaryKey": true
    },
    {
      "name": "full_name",
      "type": "TEXT",
      "postgresType": "TEXT",
      "nullable": false,
      "primaryKey": false
    },
    ...
  ]
}
### Получение схемы таблицы
GET /api/v1/dynamic-tables/{tableName}
Ответ:
[
  {
    "tableName": "person_name",
    "userFriendlyName": "Клиенты",
    "columnCount": 3
  },
  {
    "tableName": "users_info",
    "userFriendlyName": "Юзеры",
    "columnCount": 5
  }
]

### Создание записи
POST /api/v1/dynamic-tables/data/users_info
Content-Type: application/json

{
  "full_name": "Тилекбекова",
  "age_user": 21,
  "is_active": true
}

### Получение всех записей (пагинация)
GET /api/v1/dynamic-tables/data/users_info?page=0&size=10

### Получение записи по ID
GET /api/v1/dynamic-tables/data/users_info/3

### Обновление записи
PUT /api/v1/dynamic-tables/data/users_info/3
Content-Type: application/json

{
  "full_name": "Адина Тилекбекова",
  "age_user": 23
}

### Удаление записи
DELETE /api/v1/dynamic-tables/data/users_info/2

Проектные решения и допущения
При создании таблицы автоматически добавляется колонка id BIGSERIAL PRIMARY KEY.

Типы данных ограничены стандартными PostgreSQL типами: TEXT, INTEGER, BOOLEAN, и др.

Названия колонок проходят валидацию (SQL-инъекции невозможны).

Для чтения и записи используются JdbcTemplate для гибкости.

Метаданные схемы сохраняются в служебной таблице table_schemas.

Все изменения производятся в рамках транзакции (через @Transactional).

Любые ошибки возвращаются в едином формате через глобальный @ControllerAdvice.







}
