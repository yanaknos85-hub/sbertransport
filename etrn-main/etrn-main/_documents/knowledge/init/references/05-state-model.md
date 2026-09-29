# Статусная модель Этапа 1 (выжимка)

**Источник:** Confluence pageId=24438257720

---

## 1. Объекты статусной модели (4 уровня)

| Уровень | Объект | Назначение |
|---------|--------|------------|
| Карточка ЭТрН | etrn_card | Общее состояние обработки |
| Титул | etrn_title (T1/T2/T3/T4) | Наличие и состояние титула |
| Задача подписи | etrn_signing_operation (T3) | Состояние попытки подписания |
| Блокировка | etrn_title_lock (T3) | Предотвращение одновременного подписания |

**Принцип:** Карточка НЕ получает отдельные статусы для: получения пакета, валидации, открытия modal, криптооперации. Это технические события / состояния дочерних объектов.

## 2. Основной переход (шаги 1–10)

| Шаг | Card | T1 | T2 | T3 | Signing operation |
|-----|------|----|----|----|-------------------|
| 1 | IDENTIFIED | EXPECTED | EXPECTED | EXPECTED | — |
| 2 | WAIT_KORUS_DATA | EXPECTED/AVAILABLE | EXPECTED | EXPECTED | — |
| 3 | WAIT_CONDITIONS | AVAILABLE | EXPECTED/WAIT_EXTERNAL_PARTY | AVAILABLE/EXPECTED | — |
| 4 | READY_FOR_BANK_ACTION | Подписан | Подписан | READY_TO_SIGN | — |
| 5 | READY_FOR_BANK_ACTION | Без изм. | Без изм. | READY_TO_SIGN + lock | — |
| 6 | READY_FOR_BANK_ACTION | Без изм. | Без изм. | SIGNING | CREATED → SIGNING |
| 7 | WAIT_KORUS_CONFIRMATION | Без изм. | Без изм. | SIGNED_LOCALLY | SIGNED_LOCALLY |
| 8 | WAIT_KORUS_CONFIRMATION | Без изм. | Без изм. | SENT_TO_OPERATOR | SENDING_TO_KORUS |
| 10 | PROCESS_COMPLETED | Без изм. | Без изм. | Завершён | Нет активных |

**Примечание:** PROCESS_COMPLETED может устанавливаться в одной транзакции после ACCEPTED_BY_KORUS.

## 3. Правила блокировки Т3 (lock)

- При открытии modal: card=READY_FOR_BANK_ACTION, T3=READY_TO_SIGN, lock=ACTIVE
- status карточки НЕ меняется на «В работе»
- T3 status НЕ меняется до запуска подписания
- Задача подписи ещё не создаётся
- Блокируется только Т3, не вся карточка
- Второй пользователь не может подписать этот Т3
- Lock TTL — конфигурируемый (обсуждается 5 мин)
- Запрос на подписание — повторно проверяет lock и readiness

## 4. Ошибки

**Ошибка валидации:** Данные КОРУС → validation → blocking=true → card WAIT_CONDITIONS, T3 WAIT_CONDITIONS. Фиксируется в snapshot, event, validation_run, validation_issue.

**Ошибка локального подписания:** operation SIGNING→ERROR → T3 READY_TO_SIGN → card READY_FOR_BANK_ACTION. Старая задача в истории; новая — с новым ID.

**Отклонение КОРУС:** operation REJECTED_BY_KORUS → T3 REJECTED → card WAIT_CONDITIONS, blocking=true. Перевод REJECTED→SIGNING запрещён.

**OUTCOME_UNKNOWN:** operation OUTCOME_UNKNOWN → T3 SENT_TO_OPERATOR → card WAIT_KORUS_CONFIRMATION. Запрещено: повторная УКЭП, новая задача, возврат в очередь.

## 5. Завершение Этапа 1

Условия: Т3 подписан УКЭП **AND** отправлен КОРУС **AND** КОРУС подтвердил приём.

После завершения:
- Card: PROCESS_COMPLETED
- T3: ACCEPTED_BY_OPERATOR
- Operation: ACCEPTED_BY_KORUS
- ЭТрН исключена из рабочего списка
- Все данные + история сохранены в БД
- Физическое удаление НЕ выполняется