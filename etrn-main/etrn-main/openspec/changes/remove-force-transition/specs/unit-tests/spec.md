## REMOVED Requirements

### Requirement: EtrnServiceImpl — force-transition валидация
- **WHEN** вызывается `forceTransition()` с недопустимым исходным статусом
- **THEN** выбрасывается `InvalidTransitionException`

→ **REMOVED** — метод `forceTransition` удалён из `EtrnServiceImpl`.

### Requirement: EtrnServiceImpl — force-transition без причины
- **WHEN** вызывается `forceTransition()` с пустым `reason`
- **THEN** выбрасывается `BadRequestException`

→ **REMOVED** — метод `forceTransition` удалён из `EtrnServiceImpl`.
