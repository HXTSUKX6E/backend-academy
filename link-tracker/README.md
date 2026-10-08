# LinkTracker

LinkTracker – Telegram-бот, который отслеживает изменения на веб-страницах и оперативно информирует пользователя о них.

Полезную для разработки проекта информацию вы можете найти в файле [HELP.md](./HELP.md).

## Запуск проекта

**1. Создать файл `.env` в корне проекта** со следующими параметрами:

```
TELEGRAM_TOKEN=<your-token>
SCRAPPER_DB_URL=jdbc:postgresql://localhost:5432/scrapper
SCRAPPER_DB_USERNAME=postgres
SCRAPPER_DB_PASSWORD=postgres
GITHUB_TOKEN=<your-token>
STACKOVERFLOW_KEY=<key>
STACKOVERFLOW_ACCESS_KEY=<access-key>
VALKEY_HOST=localhost
VALKEY_PORT=6379

# AI Agent — суммаризация через Hugging Face (опционально)
HUGGING_FACE_API_KEY=<hf_...>
```

### Переменные окружения

|         Переменная         |         Сервис          |                            Назначение                            |
|----------------------------|-------------------------|------------------------------------------------------------------|
| `TELEGRAM_TOKEN`           | bot                     | Токен Telegram-бота                                              |
| `SCRAPPER_DB_URL`          | scrapper                | JDBC URL PostgreSQL                                              |
| `SCRAPPER_DB_USERNAME`     | scrapper                | Логин PostgreSQL                                                 |
| `SCRAPPER_DB_PASSWORD`     | scrapper                | Пароль PostgreSQL                                                |
| `GITHUB_TOKEN`             | scrapper                | Personal Access Token GitHub API                                 |
| `STACKOVERFLOW_KEY`        | scrapper                | API-ключ StackExchange                                           |
| `STACKOVERFLOW_ACCESS_KEY` | scrapper                | Access token StackExchange                                       |
| `VALKEY_HOST`              | scrapper                | Хост Valkey/Redis                                                |
| `VALKEY_PORT`              | scrapper                | Порт Valkey/Redis                                                |
| `KAFKA_BOOTSTRAP_SERVERS`  | scrapper, bot, ai-agent | Адреса Kafka-брокеров                                            |
| `SCHEMA_REGISTRY_URL`      | scrapper, bot, ai-agent | URL Confluent Schema Registry                                    |
| `BOT_PROTOCOL`             | scrapper                | Протокол отправки в бот: `kafka-outbox`, `kafka`, `rest`, `grpc` |
| `HUGGING_FACE_API_KEY`     | ai-agent                | API-ключ Hugging Face для суммаризации                           |

Токен бота: **8535763156:AAFHtRpUuKkG8xRJtG_oVvwfw-PesGkeCYg**

**2. Запустить инфраструктуру через Docker Compose:**

```bash
# Только инфраструктура (postgres, kafka, valkey, schema-registry)
docker compose up -d postgres kafka1 kafka2 kafka3 schema-registry valkey1 valkey2 valkey3 valkey-cluster-init

# Полный стек включая приложения и мониторинг
docker compose up -d
```

Это поднимет: PostgreSQL, Kafka (3 ноды), Schema Registry, **кластер Valkey из 3 нод**, а также scrapper, bot, Prometheus и Grafana.

**3. Запустить приложение:**

```bash
mvn spring-boot:run
```

**4. Для запуска всех тестов (e2e) необходим Docker Desktop.**

Бот доступен: [@link_tracker_26_bot](https://t.me/link_tracker_26_bot)

---

## Мониторинг (Prometheus + Grafana)

|   Сервис   |                     URL                      |
|------------|----------------------------------------------|
| scrapper   | `:8081/metrics`                              |
| bot        | `:8011/metrics` (выделенный management-порт) |
| Prometheus | `localhost:9090`                             |
| Grafana    | `localhost:3000` (admin/admin)               |

### Кастомные метрики

**Scrapper:** `links_on_track_total` (Gauge, по доменам github/stackoverflow), `request_duration_ms` (Histogram, scope: external\_source/database), `api_requests_total` (Counter).

**Bot:** `telegram_requests_total` (Counter, label `request_type=command|message`), `command_requests_total` (Counter, label `command`), `command_duration_ms` (Histogram, вызовы Scrapper API), `sent_notification_total` (Counter).

### Дашборды Grafana

- **Standard Metrics (RED)** — параметр `application` (bot/scrapper): Rate, Errors, Duration (P50/P95/P99), JVM Heap и Non-Heap память
- **Business Metrics** — фильтр по приложению: сообщения/с, активные ссылки по доменам, перцентили latency scrape-операций и команд бота, счётчики уведомлений

PromQL-запросы для всех визуализаций — [`example_pql.txt`](./example_pql.txt). Настроен Grafana Alert на высокое потребление JVM Heap (>512 МБ).

---

## AI Agent Service

```
Scrapper → link.raw-updates → AI Agent → link.processed-updates → Bot
```

Фильтрация → суммаризация (Hugging Face, `facebook/bart-large-cnn`) → приоритизация (HIGH/MEDIUM/LOW по ключевым словам) → группировка обновлений одного чата в окне `grouping.window-ms` (30 с). Настройки — в `ai-agent/src/main/resources/application.yaml`.

---

## Кэширование (Valkey)

Scrapper API кэширует ответ `GET /links` в Valkey (Redis-совместимая БД).

### Как работает

|              Событие              |                    Действие с кэшем                    |
|-----------------------------------|--------------------------------------------------------|
| `GET /links` (первый вызов)       | Результат сохраняется в кэш с ключом `links::<chatId>` |
| `GET /links` (повторный вызов)    | Возвращается из кэша без обращения к БД                |
| `POST /links` (добавление ссылки) | Кэш для данного `chatId` инвалидируется                |
| `DELETE /links` (удаление ссылки) | Кэш для данного `chatId` инвалидируется                |

### Конфигурация

Все параметры Valkey задаются в `application.yaml` или через переменные окружения:

```yaml
spring:
  data:
    redis:
      host: ${VALKEY_HOST:localhost}   # хост Valkey
      port: ${VALKEY_PORT:6379}        # порт Valkey

app:
  cache:
    links-ttl: ${CACHE_LINKS_TTL:PT5M}           # TTL записей в кэше (по умолчанию 5 минут)
    client-side-enabled: ${CACHE_CLIENT_SIDE_ENABLED:true}  # Lettuce CSC, default включён
```

Для подключения к **кластеру** (используется в docker-compose) задайте:

```
SPRING_DATA_REDIS_CLUSTER_NODES=valkey1:6379,valkey2:6379,valkey3:6379
```

### Кластер Valkey в docker-compose

Docker Compose разворачивает отказоустойчивый кластер из **3 мастер-нод**:

```
valkey1  →  порт 7001
valkey2  →  порт 7002
valkey3  →  порт 7003
```

Кластер создаётся автоматически контейнером `valkey-cluster-init` после старта всех нод.
Допускает отказ одной из нод без потери доступности к остальным шардам.

---

## Client Side Caching (CSC)

По умолчанию используется двухуровневый кэш на базе [Lettuce Client-Side Caching](https://valkey.io/topics/client-side-caching/):

| Уровень |                 Хранилище                  |         Доступ          |
|---------|--------------------------------------------|-------------------------|
| L1      | Локальный `ConcurrentHashMap` (in-process) | ~мкс, без сети          |
| L2      | Valkey/Redis                               | ~мс, сетевой round-trip |

**Как работает инвалидация L1:**

Lettuce отправляет `CLIENT TRACKING ON BCAST PREFIX links::` при старте. Когда `@CacheEvict`
удаляет ключ из Valkey, сервер рассылает `__redis__:invalidate` всем подписчикам с совпадающим
префиксом — Lettuce получает сообщение и автоматически удаляет ключ из локальной карты.
Это обеспечивает согласованность L1 **во всех экземплярах** приложения без лишней координации.

Отключить CSC и использовать только Redis L2:

```
CACHE_CLIENT_SIDE_ENABLED=false
```

---

## Нагрузочные тесты

Тест `CacheLoadTest` (тег `load`) измеряет среднее время `GET /links` в трёх сценариях:

- **No cache** — каждый запрос идёт в PostgreSQL
- **Redis L2** — ответ из Valkey
- **CSC L1** — ответ из локального `ConcurrentHashMap`

Запуск:

```bash
./mvnw test -pl scrapper -Dtest=CacheLoadTest -Djacoco.skip=true
```

Отчёт записывается в `scrapper/target/cache-performance-report.md`.

---

## Команды разработки

```bash
# Сборка без тестов
./mvnw package -DskipTests=true -Djacoco.skip=true

# Все тесты (требует Docker Desktop)
./mvnw verify

# Тесты scrapper
./mvnw verify -pl scrapper

# Тесты кэша (стандартный Redis)
./mvnw test -pl scrapper -Dtest=ValkeyCacheTest

# Тесты Client Side Caching
./mvnw test -pl scrapper -Dtest=ClientSideCacheTest

# Нагрузочный тест
./mvnw test -pl scrapper -Dtest=CacheLoadTest -Djacoco.skip=true

```

