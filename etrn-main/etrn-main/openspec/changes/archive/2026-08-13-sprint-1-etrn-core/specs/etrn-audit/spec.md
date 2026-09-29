# Spec: etrn-audit

## MODIFIED Requirements

### Requirement: Запись аудита
Система SHALL записывать каждое действие над ЭТрН в `etrn_audit`.

- **Поле `action`:** `VARCHAR(512)`, русский язык
  - Пример: `"ЭТрН создана"`, `"Установлена блокировка (lock) пользователем <userId>"`, `"ЭТрН обновлена"`
- **Поле `details`:** `VARCHAR(1024)`, nullable — текстовое описание
- **FK:** `etrn_id → etrn.id`
- **Индиции:** `idx_etrn_audit_etrn_id`, `idx_etrn_audit_created_at`

#### Scenario: Создание
- **WHEN** Создана новая ЭТрН
- **THEN** `etrn_audit.action = "ЭТрН создана"`

#### Scenario: Подписание
- **WHEN** Блокировка установлена
- **THEN** `etrn_audit.action = "Установлена блокировка (lock) пользователем <userId>"`

---

### Requirement: Чтение аудита
Система SHALL возвращать историю действий по `etrn_id`.

#### Scenario: Запрос истории
- **WHEN** GET /api/etrn-cargo/{id}/audit
- **THEN** 200 + список `etrn_audit` по `created_at DESC`