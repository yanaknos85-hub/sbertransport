# Spec: etrn-korus

## OUT-OF-SCOPE

> КОРУС-интеграция вынесена в отдельную задачу. Для неё будет создана отдельная спецификация.

### Требования, вынесенные в отдельную задачу
- `FETCH_ETRN_STATE(humanReadableId)` — адресная синхронизация с КОРУС
- `KorusClient` (FeignClient-интеграция)
- Force-transition из `OUTCOME_UNKNOWN` / `ERROR` (reconciliation)
