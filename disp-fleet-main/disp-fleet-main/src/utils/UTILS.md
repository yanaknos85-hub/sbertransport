# Утилиты utils

Данный модуль содержит набор вспомогательных функций для работы с данными, форматированием, валидацией и другими общими задачами.

## Содержание

- [Преобразование и форматирование](#преобразование-и-форматирование)
- [Работа с хранилищем](#работа-с-хранилищем)
- [Работа с URL](#работа-с-url)
- [Валидация и проверки](#валидация-и-проверки)
- [Работа с API](#работа-с-api)
- [React](#react)
- [io-ts типы](#io-ts-типы)
- [Правила валидации полей](#правила-валидации-полей)
- [Прочие утилиты](#прочие-утилиты)

---

## Преобразование и форматирование

| Функция | Описание |
|---------|----------|
| `formatFullName(firstName, lastName, middleName)` | Форматирует ФИО как "Фамилия И. О." |
| `numberWithSpaces(x)` | Форматирует число с пробелами (разделение тысяч) |
| `transformStateNumber(value)` | Преобразует номер авто, заменяя латиницу на кириллицу |
| `normalizeRegCarNumber(value, prevValue)` | Валидирует и нормализует номер авто |
| `handleInputOnlyNumbers(e)` | Оставляет только цифры в input |

---

## Работа с хранилищем

| Функция | Описание |
|---------|----------|
| `getFiltersValues(filtersKey)` | Получает фильтры из localStorage |
| `saveFiltersValues(filtersKey, filters)` | Сохраняет фильтры в localStorage |

---

## Работа с URL

| Функция | Описание |
|---------|----------|
| `setIntoUrl(urlParams, obj, key)` | Добавляет значение в URLSearchParams (поддерживает массивы) |
| `sortLabelValue(prev, next)` | Сортирует LabeledValue по label (без учета регистра) |

---

## Валидация и проверки

| Функция | Описание |
|---------|----------|
| `isJsonString(str)` | Проверяет, является ли строка JSON |
| `isTestMode()` | Проверяет тестовый режим |
| `isTestStand()` | Проверяет, запущено ли на тестовом стенде |

---

## Работа с API

| Функция | Описание |
|---------|----------|
| `getErrorCode(error)` | Извлекает код ошибки из Axios |
| `getFilenameFromHeader(response, settings?)` | Извлекает имя файла из Content-Disposition |
| `handleClick(data, mimeType, fileName)` | Загружает файл в браузере |
| `downloadFile(response, settings?)` | Скачивает файл из ответа Axios |

---

## React

| Функция | Описание |
|---------|----------|
| `createCallableCtx(value, options)` | Создает вызываемый React контекст |

---

## io-ts типы

Типы для валидации данных из `src/utils/io-ts/`:

| Тип | Описание |
|-----|----------|
| `optional(codec)` | Необязательное поле (T \| undefined) |
| `nullable(codec)` | Nullable поле (T \| null) |
| `withValidate(codec, validate, name)` | Кастомная валидация |
| `fallback(codec, value, name)` | Значение по умолчанию |
| `numberString` | Строка → число |
| `money` | Копейки ↔ рубли |
| `mobilePhone` | Мобильный телефон |
| `epochTimestamp` | Timestamp → Date |
| `time` | Мс → минуты |
| `ISODate` | ISO строка → moment.Moment |
| `EpochMS` | Milliseconds → moment.Moment |
| `uuid` | UUID v4 строка |
| `oneOf(...variants)` | Union из literal типов |
| `ioTypeFromEnum(name, enum)` | Тип из TypeScript enum |
| `createPagination(content)` | Пагинированный ответ |
| `wrapped(typ, Cls)` | Оборачивание в класс |

---

## Правила валидации полей

Правила для Ant Design форм из `src/utils/fieldValidationRules/`:

| Правило | Описание |
|---------|----------|
| `required` | Обязательное поле |
| `email` | Email |
| `checkStateNumber` | Госномер |
| `checkVin` | VIN |
| `checkPhoneNumber` | Телефон |
| `maxInt(name?)` | Максимальное значение (авто для Cost/Time/Year) |
| `minInt(name?)` | Минимальное значение (авто для Year) |
| `minManufactureYear(n)` | Минимальный год производства |
| `validatorStartDate(form, endDateKey)` | Валидация даты начала |
| `validatorEndDate(form, startDateKey)` | Валидация даты окончания |

---

## Прочие утилиты

| Функция | Описание |
|---------|----------|
| `indexById(items)` | Создает индекс по id из массива |
| `_env(name)` | Получение значения переменной окружения |
| `jwtDecode(token)` | Декодирует JWT токен |
| `b64EncodeUnicode(str)` | Base64 кодирование (UTF-8) |
| `b64DecodeUnicode(str)` | Base64 декодирование (UTF-8) |
| `isObject(value)` | Проверка на объект |
| `isFilledObject(value)` | Проверка на непустой объект |
| `isEmptyObject(value)` | Проверка на пустой объект |
| `isEmptyArray(value)` | Проверка на пустой массив |
| `isEqualArrays(arr1, arr2)` | Сравнение массивов |
| `clearSymbols(value)` | Очистка строки от символов |
| `inputCleaner(event, form)` | Очистка и валидация input |
| `includesByLowerCaseAndSpaces(first, second)` | Вхождение без учета регистра |
| `startsWithIgnoreCase(source, target)` | Начало строки без учета регистра |
| `toWildcardRegexp(input)` | Wildcard → регулярное выражение |
| `ignore()` | Функция-заглушка |
| `preventDefault(event)` | Предотвращает Enter в input |
| `deepMerge(source, target)` | Глубокое слияние объектов |
| `getErrorMessage(error)` | Сообщение об ошибке из Axios |
| `uuid()` | Генерация UUID v4 |
