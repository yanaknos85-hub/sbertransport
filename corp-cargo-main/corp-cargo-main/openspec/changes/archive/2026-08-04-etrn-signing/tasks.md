# Tasks: ЭТрН — Подписание

## 1. Рабочая очередь (WorklistTable)

- [ ] 1.1 Создать `EtrnSignature.tsx` — страница-обёртка с таблицей и фильтрами
- [ ] 1.2 Создать `components/Table/useTableFields.tsx` — хук для колонок таблицы
- [ ] 1.3 Создать `components/Filters/EtrnFilters.tsx` — поиск по ID + кнопка «Фильтры»
- [ ] 1.4 Подключить `POST /etrn-cargo/list` для загрузки данных таблицы
- [ ] 1.5 Реализовать пагинацию (страницы по 20 записей)
- [ ] 1.6 При клике на номер ЭТрН — открывать `EtrnModal` с `cardId = record.id`
- [ ] 1.7 При пустом списке — отображать «Нет документов к подписанию»
- [ ] 1.8 Создать `EtrnSignature.test.tsx` — тесты страницы

## 2. Модальная карточка (EtrnModal)

- [ ] 2.1 Создать `components/Card/EtrnModal.tsx` — модальное окно
- [ ] 2.2 Подключить `useAcquireLock()` для блокировки при открытии (PUT /lock)
- [ ] 2.3 Подключить `useReleaseLock()` для разблокировки при закрытии (DELETE /lock)
- [ ] 2.4 Подключить `useEtrnCard()` для получения данных карточки (GET /etrn-cargo/{id})
- [ ] 2.5 Обработка 409 Conflict — открыть в read-only с сообщением
- [ ] 2.6 Tabs: general, documents, checks, history (last three — заглушки)
- [ ] 2.7 Блокировка на 5 минут: если пользователь не закрыл карточку, блокировка истекает
- [ ] 2.8 Создать `EtrnModal.test.tsx` — тесты модалки

## 3. Компоненты карточки

- [ ] 3.1 Создать `components/Card/components/TitleChain/TitleChain.tsx` — цепочка титулов T1–T4
  - [ ] 3.1.1 При пустой цепочке — показать «Ожидание данных от Корус»
- [ ] 3.2 Создать `components/Card/components/ParticipantBlock/ParticipantBlock.tsx` — участники
- [ ] 3.3 Созратить `components/Card/components/PepBlock/PepBlock.tsx` — ПЭП
- [ ] 3.4 Создать `components/Card/components/CargoAndRoute/CargoAndRoute.tsx` — груз и маршрут
- [ ] 3.5 Для каждого компонента — создать `__tests__` файлы и `.styles.module.scss`

## 4. API-слой

- [ ] 4.1 Создать `src/api/etrn-signature/etrn-signature.ts` — хуки:
  - [ ] 4.1.1 `useEtrnCard(cardId)` — GET /etrn-cargo/{id}
  - [ ] 4.1.2 `useAcquireLock()` — PUT /etrn-cargo/{cardId}/lock
  - [ ] 4.1.3 `useReleaseLock()` — DELETE /etrn-cargo/{cardId}/lock
- [ ] 4.2 Создать `src/api/etrn-signature/etrn-signature.test.ts` — тесты API-хуков

## 5. Хуки и сторы

- [ ] 5.1 Создать `hooks/useEtrnQuery.ts` — хук запроса данных ЭТрН
- [ ] 5.2 Создать `hooks/useEtrnQuery.test.tsx` — тесты хука
- [ ] 5.3 Создать MobX стор для работы с карточкой и lock в `components/Card/stores/`

## 6. Стилизация и иконки

- [ ] 6.1 Создать `EtrnSignature.styles.module.scss` — стили страницы
- [ ] 6.2 Создать `components/Card/styles.module.scss` — стили модалки
- [ ] 6.3 Добавить иконки в `components/Card/icons/` — cargo, truck, route, cargo, openBox, closedBox, mcd

## 7. Локализация

- [ ] 7.1 Добавить переводы для ЭТрН в `src/i18n/ru/index.ts`
- [ ] 7.2 Создать `EtrnSignature.i18n.ts` — переводы для модуля

## 8. Тестирование

- [ ] 8.1 Написать юнит-тесты для всех компонентов
- [ ] 8.2 Написать интеграционные тесты для EtrnModal (lock flow)
- [ ] 8.3 Убедиться, что все тесты проходят
