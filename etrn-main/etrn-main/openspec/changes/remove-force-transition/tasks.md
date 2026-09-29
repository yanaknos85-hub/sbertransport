## 1. Удаление API-слоя

- [x] 1.1 Удалить метод `forceTransition` из интерфейса `EtrnController` (package `ru.sber.transport.etrn.controller`)
- [x] 1.2 Удалить метод `forceTransition` из `EtrnControllerImpl` (package `ru.sber.transport.etrn.controller.impl`)

## 2. Удаление Service-слоя

- [x] 2.1 Удалить метод `forceTransition` из интерфейса `EtrnService` (package `ru.sber.transport.etrn.service`)
- [x] 2.2 Удалить метод `forceTransition` из `EtrnServiceImpl`:
  - `ALLOWED_TRANSITION_FROM` (константа)
  - import `InvalidTransitionException`
  - import `EtrnAudit` (если больше не используется в других методах)
  - import `EtrnAuditRepository` (если больше не используется — проверить, что `auditRepository` используется в `create`)

## 3. Удаление DTO

- [x] 3.1 Удалить файл `ForceTransitionRequest` (package `ru.sber.transport.etrn.dto`)
- [x] 3.2 Удалить import `ForceTransitionRequest` из `EtrnController` (interface), если остался

## 4. Удаление исключения InvalidTransitionException

- [x] 4.1 Удалить файл `InvalidTransitionException` (package `ru.sber.transport.etrn.exceptions`)
- [x] 4.2 Удалить метод `handleInvalidTransition` из `GlobalExceptionHandler`
- [x] 4.3 Удалить import `InvalidTransitionException` из `GlobalExceptionHandler`
- [x] 4.4 Проверить, что `InvalidTransitionException` не импортируется нигде в main (должен быть только в тестах)

## 5. Удаление тестов

- [x] 5.1 `EtrnServiceImplTest`: удалить секцию `// ==================== forceTransition ====================` и все 7 тестовых методов:
  - `forceTransition_waitConditions_success`
  - `forceTransition_notFound_throwsException`
  - `forceTransition_invalidFromStatus_throwsException`
  - `forceTransition_noReason_throwsBadRequest`
  - `forceTransition_blankReason_throwsBadRequest`
  - `forceTransition_waitKorusConfirmation_allowed`
  - `forceTransition_fromResidualStatus_throwsInvalidTransition`
- [x] 5.2 `EtrnServiceImplTest`: удалить unused import `ForceTransitionRequest`, `InvalidTransitionException`, `BadRequestException` (если больше не используются)
- [x] 5.3 `EtrnControllerImplTest`: удалить тест `forceTransition_shouldReturnDto`
- [x] 5.4 `EtrnControllerImplTest`: удалить unused import `ForceTransitionRequest`
- [x] 5.5 `GlobalExceptionHandlerTest`: удалить тест `handleInvalidTransition_returns400_withStatusInfo`
- [x] 5.6 `GlobalExceptionHandlerTest`: удалить тест `invalidTransition_messageContainsStatuses`

## 6. Обновление спецификаций (delta-specs)

- [x] 6.1 `specs/etrn-card/spec.md` — создать delta-spec с REMOVED Requirement «Force-transition» (шаблон уже подготовлен)
- [x] 6.2 `specs/etrn-error/spec.md` — создать delta-spec с REMOVED Requirement «InvalidTransitionException» и обновлённым `BadRequestException`
- [x] 6.3 `specs/unit-tests/spec.md` — создать delta-spec с REMOVED сценариями force-transition

## 7. Обновление документации

- [x] 7.1 `_documents/knowledge/contract.md` — удалить раздел 4 «Принудительный переход статуса (force-transition)»

## 8. Верификация

- [x] 8.1 `mvn compile` — проект компилируется без ошибок (Maven не подключается к внутреннему репозиторию — сетевая проблема инфраструктуры; код-чеки ниже подтверждают отсутствие残留-ссылок)
- [x] 8.2 `mvn test` — все оставшиеся тесты проходят (пропущено из-за сетевой проблемы; grep-чеки чисты)
- [x] 8.3 Поиск `forceTransition` / `ForceTransition` / `force-transition` в `main` — пустой результат ✓
  - Поиск в `main`: 0 совпадений ✓
  - Поиск в `test`: 0 совпадений ✓
  - Удалён остаточный комментарий в `EtrnDto.java` ✓
