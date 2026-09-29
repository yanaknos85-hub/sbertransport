## REMOVED Requirements

### Requirement: InvalidTransitionException
- **WHEN** Недопустимый force-transition (currentStatus не в allowlist)
- **THEN** 400 + тело: `"Недопустимый переход из статуса {from} в {to}"`

→ **REMOVED**

### Requirement: BadRequestException — сообщение о force-transition
- **WHEN** Некорректные параметры запроса
- **THEN** 400 + тело: `"Некорректный статус: {status}"` / `"Поле reason обязательно для force-transition"`

→ **REMOVED** ("Поле reason обязательно для force-transition")
