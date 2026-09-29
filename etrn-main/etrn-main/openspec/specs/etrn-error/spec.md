# Spec: etrn-error

## Purpose

Определение требований к обработке и возврату ошибок в сервисе ЭТрН.

## Requirements

### Requirement: Обработка ошибок (русский язык)
Система SHALL возвращать ошибки с русским описанием.

#### EtrnNotFoundException
- **WHEN** ЭТрН не найдена по UUID
- **THEN** 404 + тело: `"ЭТрН {uuid} не найдена"`

#### BadRequestException
- **WHEN** Некорректные параметры запроса
- **THEN** 400 + тело: `"Некорректный статус: {status}"` / `"Поле reason обязательно для force-transition"`

#### LockConflictException
- **WHEN** Повторная блокировка при активном lease (чужой userId)
- **THEN** 409 + тело: `"Карточка уже заблокирована"` + `LockConflictResponse { message, lockedBy, lockUntil }`

#### InvalidTransitionException
- **WHEN** Недопустимый force-transition (currentStatus не в allowlist)
- **THEN** 400 + тело: `"Недопустимый переход из статуса {from} в {to}"`

#### UserNotFoundException
- **WHEN** Пользователь не найден в реплицированной таблице `employee`
- **THEN** 404 + тело: `"Пользователь с ID {userId} не найден"`

---

### Requirement: Валидация входа
Система SHALL валидировать входящие данные.

#### Scenario: Обязательные поля
- **WHEN** `humanReadableId` пустой или null в create-запросе
- **THEN** 400 + `"Поле humanReadableId обязателен"`
