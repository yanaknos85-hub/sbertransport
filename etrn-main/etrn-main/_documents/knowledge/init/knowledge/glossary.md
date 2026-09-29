# Словарь терминов проекта ЭТрН

## Основные понятия

| Термин | Пояснение |
| --- | --- |
| **ЭТрН** | Электронная транспортная накладная; одна карточка объединяет цепочку Т1–Т4. Одна карточка = одна ЭТрН (инвариант). |
| **Т1 / Т2 / Т3 / Т4** | Последовательные титулы отправителя, перевозчика, получателя и перевозчика. Сокращение: Т1 — Титул 1, Т2 — Титул 2, Т3 — Титул 3, Т4 — Титул 4. В Этап 1 Банк подписывает только Т3. |
| **Титул** | Часть электронной транспортной накладной, которую формирует, заполняет или подписывает определённый участник перевозки. Каждый титул отражает отдельный этап оформления ЭТрН. |
| **ЦС / ЦС-Радар** | Внешняя система Банка, передающая POST /cs/title3 с идентификаторами ЭТрН и внешним routeContext. |
| **КОРУС** | Оператор ЭДО (система КОРУС) — источник данных/статусов Т1–Т3 и получатель подписанного Т3. |
| **etrn-service** | Владелец агрегата ЭТрН: карточки, титулов, readiness, lock, signing lifecycle, истории. Внутренний сервис (доменный). |
| **ПЭП / основание приёмки** | Доверенное событие, фиксирующее факт приёмки. HTTP-вызов сам по себе не заменяет юридическую модель ПЭП. |
| **УКЭП** | Усиленная квалифицированная электронная подпись центрального подписанта. |
| **МЧД** | Машиночитаемая доверенность, подтверждающая полномочия подписанта. |
| **routeContext** | Внешний контекст доставки из ЦС: route.id, route.number, route.dateTimeCreate. Не является маршрутом АС СберТранспорт. |
| **Readiness** | Результат проверки, что Т3 можно допустить к подписанию. |
| **Lease lock** | Краткоживущая блокировка Т3 при открытии окна подписания. |

## Статусная модель

### Уровни

| Уровень | Описание |
| --- | --- |
| **card** | Статус карточки ЭТрН (верхнеуровневый) |
| **title** | Статус конкретного титула Т1–Т4 |
| **signing operation** | Статус операции подписи |
| **lock** | Техническое состояние блокировки (не является card status) |

### Последовательность Stage 1

1. **card**: IDENTIFIED → WAIT_KORUS_DATA → WAIT_CONDITIONS / READY_FOR_BANK_ACTION → WAIT_KORUS_CONFIRMATION → PROCESS_COMPLETED
2. **T3**: EXPECTED → AVAILABLE/WAIT_CONDITIONS → READY_TO_SIGN → SIGNING → SIGNED_LOCALLY → SENT_TO_OPERATOR → ACCEPTED_BY_OPERATOR
3. **signing operation**: CREATED → SIGNING → SIGNED_LOCALLY → SENDING_TO_KORUS → ACCEPTED_BY_KORUS (ветвления: ERROR / REJECTED / OUTCOME_UNKNOWN)
4. **lock**: ACQUIRING/ACTIVE/LOCKED_BY_OTHER/EXPIRED/RELEASED — техническое, не card status

## Системы-участники

| Система | Роль |
| --- | --- |
| **ЦС / ЦС-Радар** | Инициирует событие: передаёт POST /cs/title3 с идентификаторами ЭТрН и внешним routeContext |
| **КОРУС** | Оператор ЭДО: источник данных/статусов, подтверждение подписи |
| **etrn-service** | Владелец карточки, титулов, readiness, lock, signing lifecycle, истории |

## Ключевые артефакты

| Артефакт | Описание                                                                        |
| --- |---------------------------------------------------------------------------------|
| **etrn** | Единая карточка ЭТрН; содержит id, status, completeness, scenario, version      |
| **etrn_identifier** | Идентификатор: type, value, source, scope, strength, primary                    |
| **etrn_route_context** | Внешний контекст доставки (не маршрут АС СБТ)                                   |
| **etrn_stage_execution** | Исполнение этапа: STAGE_1_T3, state, started/completed                          |
| **etrn_title** | Запись титула: id (ЭТрН) + titleType — уникальный активный                      |
| **etrn_document** | Документ: type (ON_TRNACLGRPO), КНД 1110341, format/version/correction/checksum |
| **etrn_party / etrn_location / etrn_cargo / etrn_acceptance** | Нормализованные данные Т3                                                       |
| **etrn_validation_run / etrn_validation_issue / etrn_validation_condition** | Результаты проверок                                                             |
| **etrn_title_lock** | Lock: token, user/session, expires/released                                     |
| **etrn_signing_operation** | Операция подписи: request key, state, document/checksum/validation/lock refs    |
| **etrn_signature** | Метаданные подписи: signature hash, storage ref, cert/MChD                      |
| **etrn_event / etrn_error / etrn_outbox** | Аудит, события, outbox                                                          |

## Бизнес-роли

| Роль | Описание |
| --- | --- |
| **Подписант ЭТрН** | Пользователь, выполняющий УКЭП-подписание Т3 |
| **Логист / наблюдатель** | Контролирует ожидания, SLA и историю |
| **Поддержка L2/L3** | Диагностика correlation, retries, validation, unknown outcome |
| **Центральный подписант УКЭП** | Проверяет готовый Т3 и запускает подписание |

## Коды готовности и ошибок

| Код | Описание |
| --- | --- |
| IDENTITY_CONFLICT | Blocking issue; без слияния |
| NOT_READY | 409 / список условий |
| LOCKED_BY_OTHER | 409 + expiry / владелец |
| LOCK_EXPIRED | 409; перезахватить |
| CHECKSUM_CHANGED | 409; перезагрузить |
| ACTIVE_OPERATION_EXISTS | 409; вернуть операцию |
| OUTCOME_UNKNOWN | 423/409; заблокировано до reconciliation |

## Дополнительно

| Термин | Пояснение |
| --- | --- |
| **АС СберТранспорт** | Система-владелец etrn-service |
| **ЦС** | Центральная система (инициатор) |
| **ЦС-Радар** | Модификация/версия ЦС |
| **Справочник юрлиц Банка** | Нормализованный источник для определения bank_is_sender/bank_is_receiver |
| **Strong/weak идентификаторы** | Степень доверия к идентификатору: strong — primary/неоспоримый, weak — справочный |
| **Scenario code** | Код сценария обработки: BANK_SENDER_ONLY, BANK_RECEIVER_ONLY, BANK_SENDER_RECEIVER и др. |
| **Feature flags** | Флаги включения функциональности: inbound CS, KORUS fetch, signing UI, submit/reconcile, completion |
| **Optimistic locking** | Версионная блокировка в EtrnRepository для защиты от конкурентных изменений |
| **Idempotent** | Идемпотентность — повтор одного входа не создаёт дубликата |
| **Durable inbox** | Надёжное хранилище входящих сообщений |