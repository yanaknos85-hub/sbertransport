# Описание процессов Этапа 1 (выжимка)

**Источник:** Confluence pageId=24438254660
**Объём:** От факта приёмки в ЦС до PROCESS_COMPLETED в АС СберТранспорт

---

## 1. Сквозной процесс

```
CS event → durable inbox → card/identifiers → targeted KORUS sync → normalize → validate → queue → lock → UKEP → send → KORUS confirmation → complete
```

## 2. Детальные процессы

### P1. Приём POST /cs/title3
- **Триггер:** ЦС зафиксировал факт приёмки
- **Вход:** OAuth2, JSON, route + etrn[]
- **Техническая валидация:** auth, размер, JSON, обязательные поля
- **Транзакция:** inbox + hash — durable до HTTP 200
- **Ответ 200:** только приём в работу, не успех
- **Повтор:** идемпотентен
- **Альтернативы:** 400 (payload), 401/403 (auth), частичный пакет (item независимы)

### P2. Разрешение идентичности и создание карточки
- Для каждого etrn[i]: strong/weak identifier candidates
- Найти/создать minimal card со status IDENTIFIED
- Сохранить routeContext (не создавать маршрут АС СБТ)
- При конфликте strong IDs → identity_conflict + blocking issue
- Создать Stage execution + логические Т1–Т4
- **Progressive enrichment:** карточка может быть минимальной

### P3. Адресная синхронизация с КОРУС
- Integration operation FETCH_ETRN_STATE
- Вызов КОРУС (не polling всех документов)
- Сохранить immutable operator snapshot
- Нормализация Т1/Т2/Т3/participants/location/cargo/acceptance
- Проверка T2 PRECEDES T3
- Событие DATA_NORMALIZED → запуск validation

### P4. Валидация и расчёт готовности
| Фаза | Проверки | Выход |
|------|----------|-------|
| IDENTIFICATION | Карточка + strong IDs однозначны | Conflict → blocking |
| FORMAT | documentType, КНД, версия, XSD | FAILED → WAIT_CONDITIONS |
| READINESS | Т1/Т2 accepted, T2→T3, Bank, location, cargo, discrepancies | Все satisfied → READY_TO_SIGN |
| PRE_SIGN | Права, сертификат, МЧД, checksum, lock, operation | Разрешение/запрет операции |

### P5. Пользовательская проверка и временная блокировка
- Подписант открывает карточку READY_FOR_BANK_ACTION
- При открытии modal → ACTIVE title lock (TTL configurable)
- Lock на Т3, не на карточку
- Card/T3 status не меняется
- Close → release; сбой → expiry
- Перед подписью → PRE_SIGN recheck + lock token

### P6. УКЭП-подписание и отправка
| Состояние | Действие | Гарантия |
|-----------|----------|----------|
| CREATED | Создать operation с idempotency key, document, checksum | Одна active |
| SIGNING | Проверить сертификат/МЧД, сформировать УКЭП | Ошибка → новая operation |
| SIGNED_LOCALLY | Сохранить signature + signed checksum | Card → WAIT_KORUS_CONFIRMATION |
| SENDING_TO_KORUS | Integration operation outbound | Повтор send ≠ новая подпись |
| OUTCOME_UNKNOWN | Timeout после send | Повторная УКЭП запрещена |

### P7. Подтверждение КОРУС и завершение
| Ответ | Изменения |
|-------|-----------|
| ACCEPTED | operation ACCEPTED_BY_KORUS → T3 ACCEPTED → PROCESS_COMPLETED → исключение из active queue |
| REJECTED | operation REJECTED → T3 REJECTED → blocking error → card WAIT_CONDITIONS |
| Неоднозначно | OUTCOME_UNKNOWN → T3 SENT_TO_OPERATOR → card WAIT_KORUS_CONFIRMATION → reconciliation |

### P8. Ошибки и компенсации
| Класс | Пример | Компенсация |
|-------|--------|------------|
| Retryable integration | KORUS timeout | Exponential retry; card WAIT_KORUS_DATA |
| Format/data | Неподдерживаемая версия | Raw сохранён; blocking issue; новая data → revalidate |
| Business condition | Т2 не подписан | WAIT_CONDITIONS; targeted sync |
| Local signing | Ошибка криптопровайдера | ERROR → T3 READY_TO_SIGN → новая operation |
| Operator rejection | КОРУС отклонил | REJECTED → исправление → новая operation |
| Unknown outcome | Разрыв после send | Reconciliation; запрет повторной подписи |
| Concurrency | Чужой lock/operation | Отказать безопасно; не менять state |

## 3. RACI

| Процесс | ЦС | etrn-service | КОРУС | Подписант | Поддержка |
|---------|----|-------------|-------|-----------|----------|
| P1 /cs/title3 | R | A/R | I | I | C |
| P2 Карточка/IDs | I | A/R | I | I | C |
| P3 Sync | I | A/R | R | I | C |
| P4 Validation | C | A/R | C | I | C |
| P5 Review/lock | I | R | I | A/R | C |
| P6 УКЭП/send | I | R | C | A/R | C |
| P7 Confirmation | I | A/R | R | I | C |
| P8 Error/reconciliation | I | R | C | I | A/R |

## 4. Критерии приёмки процессов

1. HTTP 200 → только после durable регистрации входа
2. Batch: частичный успех, карточки не смешиваются
3. routeContext → не маршрут АС СБТ
4. KORUS sync — адресный, повторяемый
5. Validation → явные conditions/issues, не business status
6. READY_TO_SIGN → только при всех 20 условиях
7. Lock — краткоживущий lease
8. Подписываемая версия закреплена checksum
9. Unknown outcome → повторная подпись запрещена
10. После ACCEPTED → данные и история сохранены