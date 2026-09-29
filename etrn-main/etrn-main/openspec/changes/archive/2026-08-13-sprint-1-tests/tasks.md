## 1. Тестовая инфраструктура

- [x] 1.1 Добавить test-зависимости в application/pom.xml (embedded-postgres, kafka-functional, awaitility)
- [x] 1.2 Создать TestCommon.java — extends KafkaTest с константами (org/emp/etrn UUID, roles, statuses)
- [x] 1.3 Создать application-test.yml с тестовой конфигурацией (ddl-auto=none, scheduling=false, security.enabled=false)

## 2. Unit-тесты для util и exceptions

- [x] 2.1 Написать ContextHelperTest — getUserId из JWT, из Authentication, null-обработка, getRoles
- [x] 2.2 Написать GlobalExceptionHandlerTest — handleNotFound, handleBadRequest, handleLockConflict, handleValidation, handleGeneral
- [x] 2.3 Написать тесты для checked exceptions — EtrnNotFoundException, BadRequestException, InvalidTransitionException, LockConflictException, UserNotFoundException
- [x] 2.4 Написать IsoLocalDateTimeSerializerTest — convert, serialize

## 3. Unit-тесты для enums

- [x] 3.1 Написать EtrnCardStatusTest — все значения имеют описания
- [x] 3.2 Написать EtrnTitleStatusTest — все значения имеют описания
- [x] 3.3 Написать SigningOperationStatusTest — все значения имеют описания
- [x] 3.4 Написать LockStatusTest — все значения имеют описания

## 4. Unit-тесты для schedulers

- [x] 4.1 Написать EtrnLockSchedulingProcessorImplTest — releaseEtrnLocks вызывает lockService.scheduleAutoUnlock()

## 5. Unit-тесты для service-имплементаций

- [x] 5.1 Написать EtrnServiceImplTest — create (новая, идемпотентная), getById (найдена, не найдена), list (разные фильтры), forceTransition (валидация, переходы)
- [x] 5.2 Написать DepartmentServiceImplTest — save, findById, delete, get, findOrCreateById
- [x] 5.3 Написать EmployeeServiceImplTest — save, delete, findById, getAuthenticatedEmployee (найден, не найден)
- [x] 5.4 Написать OrganizationServiceImplTest — save, findById, delete, get

## 6. Unit-тесты для mapper

- [x] 6.1 Написать DepartmentMapperTest — fromMessage, update
- [x] 6.2 Написать EmployeeMapperTest — fromMessage, update, employeeMessageToEmployeeEntity
- [x] 6.3 Написать OrganizationMapperTest — fromMessage с organizationGroup

## 7. Unit-тесты для database model

- [x] 7.1 Написать EtrnAuditTest — builder, noArgsConstructor, allArgsConstructor, setCreatedBy, setCreatedAt
- [x] 7.2 Написать OrganizationGroupTest — builder, default values
- [x] 7.3 Написать EtrnTest — prePersist, preUpdate, TitleEntry, Verifications, LockInfo

## 8. Integration-тесты

- [x] 8.1 Написать EtrnControllerImplTest — POST /create, GET /{id}, GET /{id} не найдена, GET /list
- [x] 8.2 Написать OrganizationListenerTest — новая организация, удаление (из другого проекта)
- [x] 8.3 Написать DepartmentListenerTest — новая организация, удаление (из другого проекта)
- [x] 8.4 Написать EmployeeListenerTest — авторизация (из другого проекта)

## 9. Верификация

- [x] 9.1 Запустить все тесты и убедиться, что все проходят
- [x] 9.2 Проверить JaCoCo покрытие — цель 85%+
- [x] 9.3 Зафиксировать результаты в proposal.md
