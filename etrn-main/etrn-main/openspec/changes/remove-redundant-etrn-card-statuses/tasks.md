## 1. Main-код

- [x] 1.1 `EtrnCardStatus` (`application/src/main/java/ru/sber/transport/etrn/enums/EtrnCardStatus.java`): удалить значения `ERROR` и `OUTCOME_UNKNOWN`, осталось ровно 6; обновить javadoc (цепочка без ошибочных веток)
- [x] 1.2 `EtrnServiceImpl` (`application/src/main/java/ru/sber/transport/etrn/service/impl/EtrnServiceImpl.java`): `ALLOWED_TRANSITION_FROM` = `WAIT_CONDITIONS`, `WAIT_KORUS_CONFIRMATION` (убрать `ERROR`, `OUTCOME_UNKNOWN`)

## 2. Тесты

- [x] 2.1 `EtrnServiceImplTest`: удалить тесты `forceTransition_errorStatus_allowed` и `forceTransition_outcomeUnknown_allowed`
- [x] 2.2 `EtrnServiceImplTest`: добавить тест force-transition из остаточного статуса (`OUTCOME_UNKNOWN` строкой в сущности) → `InvalidTransitionException` (сценарий «Переход из удалённого статуса»)
- [x] 2.3 `EtrnCardStatusTest`: добавить тест — `values()` возвращает ровно 6 статусов (containsExactly по порядку жизненного цикла)
- [x] 2.4 `TestCommon`: удалить константы `STATUS_ERROR` и `STATUS_OUTCOME_UNKNOWN`

## 3. Валидация кода

- [x] 3.1 `mvn -q compile` — без ошибок (проверка, что ссылок на удалённые значения не осталось)
- [x] 3.2 `mvn test` — все unit-тесты зелёные (145/145; `@EmbeddedPostgres` — с `-Dembedded.temp=.tmp-pg` из-за ограничений `/tmp`)
- [x] 3.3 `grep -rn "EtrnCardStatus.ERROR\|EtrnCardStatus.OUTCOME_UNKNOWN" application/src` — 0 совпадений в main и test

## 4. Документация и спеки

- [x] 4.1 `_documents/knowledge/init/knowledge/data-model.md` — модель статусов карточки: 6 значений (убрать ERROR/OUTCOME_UNKNOWN)
- [x] 4.2 `_documents/knowledge/init/references/05-state-model.md` — подтверждено соответствие: матрица карточки уже содержит ровно 6 статусов; упоминания ERROR/OUTCOME_UNKNOWN относятся к уровню операции подписания (корректно сохранены), изменений не требуется
- [x] 4.3 `openspec/specs/etrn-korus/spec.md` — убрать устаревшую строку OUT-OF-SCOPE «Force-transition из `OUTCOME_UNKNOWN` / `ERROR` (reconciliation)»
- [x] 4.4 Проверить `GIGACODE.md` §6.1 на соответствие 6 статусам (подтверждено: строка 252 — ровно 6 статусов)
- [x] 4.5 `openspec validate --change "remove-redundant-etrn-card-statuses"` — без ошибок

## 5. Преддеплойная проверка (при релизе)

- [ ] 5.1 `SELECT status, count(*) FROM etrn_cargo.etrn GROUP BY status` — убедиться, что остаточных `ERROR`/`OUTCOME_UNKNOWN` нет (при наличии — ручное согласование до деплоя)
