# User Service

Микросервис для управления пользователями: CRUD-операции.

## 📌 Основные функции
- Создание, обновление, удаление пользователей
- Валидация данных
- Логирование операций
- Интеграция с Kafka (события `UserCreated`, `UserDeleted`)

## 🚀 Быстрый старт

### Предварительные требования
- Docker Desktop
- Java 17 (или OpenJDK 17)
- склонированный и запущенный репозиторий с БД и кафкой https://github.com/tapotas1234/infrastructure

### Запуск в development-режиме
```bash
# 1. Клонировать репозиторий
git clone git@github.com:tapotas1234/user-service-rest.git
cd user-service-rest

# 2. Запустить сервис
mvn spring-boot:run
```

## 📚 API Endpoints

### POST /user-service-rest-1.0/api/users – Создать пользователя

{
"name": "Jot123434m",
"age": 23,
"email": "john@mail.ru"
}

### GET /user-service-rest-1.0/api/users – Получить пользователя

### DELETE /user-service-rest-1.0/api/users/{id} - Удалить пользователя

### PUT /user-service-rest-1.0/api/users/{id} - Обновить пользователя

{
"name": "Jot123434m",
"age": 23,
"email": "john@mail.ru"
}

## Технологический стек

 - Java 17 + Spring Boot 3
 - PostgreSQL
 - Kafka
 - Maven