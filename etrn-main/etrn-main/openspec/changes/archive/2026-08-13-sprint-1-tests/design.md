## Context

Проект `app_cargo_etrn` — Java/Spring Boot 3 сервис электронных транспортных накладных (ЭТрН). На момент начала работы проект имел 47% покрытия кода тестами (Controller, Config, DTO, Mapper, ServiceImpl были частично покрыты). Необходимость: установить надёжную тестовую инфраструктуру для безопасной итеративной разработки в соответствии со стандартами `as-sbertransport-cargo`.

## Goals / Non-Goals

**Goals:**
- Создать инфраструктуру для unit- и integration-тестирования (embedded-PostgreSQL, MockMvc, Kafka test binder)
- Написать unit-тесты для всех критичных классов service-слоя
- Написать integration-тесты для контроллеров и Kafka-листенеров
- Довести покрытие до 85%+

**Non-Goals:**
- Достижение 100% покрытия (JPA boilerplate не требует тестирования)
- Рефакторинг production-кода
- Тестирование внешних интеграций (КОРУС, ЦС)

## Decisions

1. **embedded-PostgreSQL вместо Testcontainers** — быстрее, не требует Docker, используется `ru.sber.transport.postgres:embedded-postgres` из монолита
2. **`@SpringBootTest` + `@EmbeddedPostgres`** для integration-тестов — соответствует стилю проекта, запускает embedded-PostgreSQL с заменой DataSource
3. **JUnit 5 + Mockito + AssertJ** — стандартный стек из эталонного проекта
4. **Package-private классы и методы** — тестируем через тесты в том же пакете, как в эталонном проекте
5. **`@DisplayName` на русском языке** — конвенция проекта

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| 128 тестов добавляют ~40 сек к run-time | integration-тесты с embedded-DB и Kafka отделены, unit-тесты быстрые |
| JPA-сущности (prePersist/preUpdate) не покрываются | Без JPA-контекста невозможно вызвать @PrePersist; покрытие JPA-моделей 70% достаточно |
| `EtrnApplication` (main) не покрывается | Это спринт-бутстрап, не бизнес-логика, 37% покрытия не критично |
