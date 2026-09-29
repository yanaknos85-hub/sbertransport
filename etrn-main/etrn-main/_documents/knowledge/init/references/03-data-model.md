# Модель данных Этапа 1 (выжимка)

**Источник:** Confluence pageId=24438250883
**Версия:** v2 (на основе ETRN-00-BE-01_Детальная_модель_данных_БД_v2.4)
**Назначение:** Определить Stage 1-профиль БД

---

## 1. Ключевые принципы

- Одна `etrn_card` — корень всей ЭТрН; Т1–Т4 не создают отдельные карточки
- Карточка создаётся по минимальным идентификаторам + progressive enrichment
- Raw snapshot сохраняется неизменяемо; normalised current-версии — версионно
- Бизнес-статус карточки ≠ статус титула ≠ operation ≠ lock ≠ ошибка
- Подписывается только checksum, прошедший validation
- ON DELETE RESTRICT; физическое удаление запрещено
- Рабочая очередь = вычисляемая проекция `v_etrn_signing_queue`

## 2. Полный каталог сущностей

| Сущность | Назначение | Ключевые данные |
|----------|-----------|-----------------|
| **etrn_card** | Корень ЭТрН | status, completeness, bank_role, display_number, timestamps, version |
| **etrn_identifier** | Внешние ID | type/value/source/scope/strength; ЭТрН, КОРУС, ГИС ЭПД, ЦС |
| **integration_inbox** | Durable приём HTTP | raw request, requestId, hash, source, receipt status |
| **cs_title3_batch / item** | Пакет route + etrn[] | частичный успех, дедупликация, связь с cardId |
| **etrn_route_context** | Внешний контекст ЦС | route.id, number, dateTimeCreate; БЕЗ маршрута АС СБТ |
| **etrn_stage_execution** | Выполнение Stage 1 | stage_code = STAGE_1_T3, status, completion |
| **etrn_title** | Т1/Т2/Т3/Т4 | availability, processing, bankActionRequired, currentDocument |
| **etrn_stage_title_requirement** | Требования к титулу | EXTERNAL_SIGN / BANK_SIGN |
| **etrn_document** | Версия документа | type, КНД, format/version, KORUS IDs, checksum |
| **etrn_title_link** | Связь титулов | T2 PRECEDES T3 (current) |
| **etrn_party** | Участники | sender/receiver/carrier; юрлицо Банка |
| **etrn_location** | Точка выгрузки | address, FIAS/GAR/GLN, coordinates, timezone |
| **etrn_transport_context** | Контекст Т2 (ПДн) | carrier, driver, vehicle |
| **etrn_cargo_summary / item** | Сводка/позиции груза | places, weight, units, package, condition, markings |
| **etrn_acceptance_event** | Событие приёмки ЦС | actor, occurredAt, basis_status, payload hash |
| **etrn_title3_acceptance** | Нормализованная приёмка | receiver, location, cargo, times, acceptance_status, discrepancies |
| **etrn_integration_operation** | Взаимодействие с КОРУС | operation type, retry, correlation, outcome |
| **etrn_operator_snapshot** | Неизменяемый ответ КОРУС | raw payload/hash, version, fetchedAt |
| **etrn_validation_run / issue** | Результат проверок | field_path, severity, blocking, document/basis refs |
| **etrn_title_condition** | Condition-состояние | condition code, phase, required, blocking, state |
| **etrn_title_lock** | Lease Т3 | user/session/token, acquired/expires/released |
| **etrn_signing_operation** | Попытка УКЭП | document/checksum, validation, basis, statuses, errors |
| **etrn_signature** | Метаданные подписи | signer, certificate, МЧД, basis, signed checksum |
| **etrn_event_log / state_transition** | Аудит | source, before/after, occurredAt, correlation |
| **etrn_processing_error** | Ошибки обработки | blocking, retryable, code, target |
| **etrn_outbox_event / idempotency_record** | Асинхронность | exactly-once, event publication, repeat protection |
| **etrn_artifact** | PDF/XML (необязательно) | storage ref, checksum, availability |

## 3. Кардинальности и инварианты

| Связь | Кард-ть | Инвариант |
|-------|---------|-----------|
| card → title | 1 : 1..4 | ≤1 Т1, Т2, Т3, Т4 на карточку |
| title → document | 1 : 0..N | Одна current-версия, предыдущие сохранены |
| T2 → T3 | 1 : 1 current | Для READY_TO_SIGN обязательна T2 PRECEDES T3 |
| T3 → lock | 1 : 0..N | Одновременно ≤1 active lock |
| T3 → signing_operation | 1 : 0..N | Одновременно ≤1 незавершённая |
| operation → signature | 1 : 0..1 | Подпись к конкретным document/checksum/basis |
| batch → item | 1 : 1..N | Каждый etrn[] — свой outcome |

**Идентификация:** Номер ЭТрН — НЕ внутренний PK. Все FK / deep links — internal cardId (UUID).

## 4. Условия готовности Т3 (20 условий)

| Condition code | Фаза | Смысл |
|----------------|------|-------|
| IDENTITY_RESOLVED | IDENTIFICATION | Strong IDs без конфликта |
| DOCUMENT_TYPE_T3_VALID | FORMAT | documentType = ON_TRNACLGRPO |
| KND_T3_VALID | FORMAT | КНД = 1110341 |
| FORMAT_VERSION_VALID | FORMAT | Версия в разрешённом ruleset |
| TITLE1_SIGNED | READINESS | Т1 подтверждён оператором |
| TITLE2_SIGNED | READINESS | Т2 подтверждён оператором |
| TITLE2_T3_LINK_VALID | READINESS | Т2 и Т3 одной ЭТрН |
| TITLE3_DATA_AVAILABLE | READINESS | Current-версия Т3 получена |
| BANK_RECEIVER_CONFIRMED | READINESS | bank_role = RECEIVER |
| RECEIVER_MATCHED | READINESS | Получатель = юрлицо Банка |
| UNLOADING_LOCATION_AVAILABLE | READINESS | Валидная точка выгрузки |
| ACCEPTANCE_EVENT_AVAILABLE | READINESS | Доверенное событие приёмки |
| ACCEPTANCE_BASIS_VERIFIED | READINESS | Основание прошло проверку |
| CARGO_DATA_COMPLETE | READINESS | Обязательные грузовые данные |
| NO_DISCREPANCIES | READINESS | Нет частичной приёмки/отказа/расхождений |
| TITLE3_NOT_ALREADY_SIGNED | READINESS | Нет принятой КОРУС подписи |
| SIGNER_AUTHORIZED | PRE_SIGN | Роль, сертификат, МЧД актуальны |
| DOCUMENT_CHECKSUM_UNCHANGED | PRE_SIGN | Подписывается проверенная версия |
| LOCK_VALID | PRE_SIGN | Lock принадлежит пользователю, не истёк |
| NO_ACTIVE_SIGNING_OPERATION | PRE_SIGN | Нет другой активной операции |

## 5. Транзакционные сценарии

**Приём /cs/title3:** inbox → batch/item → resolveOrCreateCard → routeContext → event → outbox FETCH_ETRN_STATE

**Sync КОРУС:** integration operation → immutable snapshot → нормализация в одной транзакции → FORMAT + READINESS validation

**Открытие окна подписи:** Проверка READY_TO_SIGN → ACTIVE lock с TTL → status не меняется

**Подписание:** PRE_SIGN recheck → signing operation → УКЭП → signature → send KORUS → ACCEPTED → Stage complete → complete projection → сохранение в БД