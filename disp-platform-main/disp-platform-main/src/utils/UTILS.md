# Утилиты utils

Данный модуль содержит набор вспомогательных функций и компонентов для работы с данными, форматированием, работой с браузерными API и другими общими задачами.

## Содержание

- [Календарь](#calendar)
- [Преобразование строк](#преобразование-строк)
- [Форматирование](#форматирование)
- [Работа с хранилищем](#работа-с-хранилищем)
- [Работа с URL](#работа-с-url)
- [Валидация и проверки](#валидация-и-проверки)
- [Работа с API](#работа-с-api)
- [Работа с React](#работа-с-react)
- [io-ts типы](#io-ts-типы)
- [Правила валидации полей](#правила-валидации-полей)
- [Прочие утилиты](#прочие-утилиты)

---

## calendar

Функции и константы для работы с календарем и датами.

| Функция | Описание |
|---------|----------|
| `monthNames` | Массив названий месяцев на русском языке |
| `monthNamesEng` | Массив названий месяцев на английском языке (верхний регистр) |
| `monthNamesShort` | Сокращенные названия месяцев на русском |
| `monthNamesEngShort` | Сокращенные названия месяцев на английском |
| `makeFirstFoundCharUppercase(str)` | Делает первую найденную букву в строке заглавной |

---

## Преобразование строк

Функции для преобразования форматов строк.

| Функция | Описание |
|---------|----------|
| `convertCamelToSnakeCase(string, uppercase = true, splitter = '_')` | Преобразует camelCase в snake_case |
| `convertSnakeToCamelCase(string)` | Преобразует snake_case в camelCase |

---

## Форматирование

Функции для форматирования данных в человекочитаемый вид.

| Функция | Описание |
|---------|----------|
| `formatDriverRating(rating, emptyValue = '-')` | Форматирует рейтинг водителя (100 → 1.00) |
| `formatFullName(firstName, lastName, middleName)` | Форматирует ФИО как "Фамилия И. О." |
| `formatPhoneNumber(str)` | Форматирует номер телефона как `+7 (XXX) XXX-XX-XX` |
| `formatRubles(amount, emptyValue = '-')` | Форматирует сумму в рублях (`1000 ₽`) |
| `getAddress(waypoint)` | Составляет полный адрес из компонентов путевой точки |
| `getFullName(human)` | Составляет полное имя из объекта человека |
| `getFullVehicle(vehicle)` | Составляет название транспорта из бренда и модели |
| `convertToRubles(sum)` | Конвертирует копейки в рубли |

---

## Работа с хранилищем

Функции для работы с localStorage.

| Функция | Описание |
|---------|----------|
| `getFiltersValues(key)` | Получает сохраненные значения фильтров из localStorage |
| `saveFiltersValues(key, filters)` | Сохраняет значения фильтров в localStorage |

---

## Работа с URL

Функции для работы с URL и параметрами запроса.

| Функция | Описание |
|---------|----------|
| `setIntoUrl(params, obj, key)` | Добавляет значение из объекта в URLSearchParams |

---

## Валидация и проверки

Функции для валидации данных и проверки условий.

| Функция | Описание |
|---------|----------|
| `isJsonString(str)` | Проверяет, является ли строка корректным JSON |
| `isTestMode()` | Проверяет, находится ли приложение в тестовом режиме |
| `isTestStand()` | Проверяет, запущено ли приложение на тестовом стенде |

---

## Работа с API

Функции для работы с HTTP-запросами и ответами.

| Функция | Описание |
|---------|----------|
| `getErrorCode(error)` | Извлекает код ошибки из ошибки Axios |
| `preparePhoneForBackend(phone, pattern)` | Преобразует номер телефона для отправки на бэкенд |
| `getFilenameFromHeader(response)` | Извлекает имя файла из header-а Content-Disposition |
| `handleClick(data, mimeType, fileName)` | Создает и инициирует загрузку файла в браузере |

---

## Работа с React

React-специфичные утилиты и хуки.

| Функция | Описание |
|---------|----------|
| `createCallableCtx(value, options)` | Создает вызываемый React контекст |

---

## io-ts типы

Библиотека io-ts типов для валидации данных.

| Функция/Тип | Описание |
|-------------|----------|
| `optional(codec)` | Создает тип (T \| undefined) для необязательных полей |
| `nullable(codec)` | Создает тип (T \| null) для nullable полей |
| `withValidate(codec, validate, name)` | Добавляет кастомную валидацию к типу |
| `fallback(codec, value, name)` | Создает тип со значением по умолчанию |
| `numberString` | Тип для строки, преобразуемой в число |
| `money` | Тип для денежных значений (копейки ↔ рубли) |
| `mobilePhone` | Тип для мобильного телефона |
| `epochTimestamp` | Тип для timestamp в Date |
| `time` | Тип для времени в минутах |
| `ISODate` | Тип для ISO даты (moment.Moment) |
| `EpochMS` | Тип для даты из milliseconds (moment.Moment) |
| `uuid` | Тип для UUID v4 строк |
| `oneOf(...variants)` | Создает union из literal типов |
| `createPagination(content)` | Создает тип для пагинированного ответа |
| `wrapped(typ, Cls)` | Оборачивает декодированное значение в класс |

---

## Правила валидации полей

Правила валидации для Ant Design форм.

| Константа/Правило | Описание |
|-------------------|----------|
| `MAX_INT` | Максимальное целое число (2147483647) |
| `MAX_INT_COST` | Максимум для полей стоимости (рубли) |
| `MAX_INT_TIME` | Максимум для полей времени (минуты) |
| `MIN_VEHICLE_MANUFACTURED_YEAR` | Минимальный год производства авто (1900) |
| `ALLOWED_VEHICLE_REGISTRY_CHARS` | Разрешенные символы для госномеров |
| `validationPatterns.*` | Регулярные выражения для форматов |
| `ValidationRules.general.*` | Правила валидации для форм |

### validationPatterns

| Паттерн | Описание |
|---------|----------|
| `stateNumberRegExp` | Госномер ТС: A123AA 123 RUS |
| `semitrailerNumberRegExp` | Госномер полуприцепа: AA12345 123 RUS |
| `vinRegExp` | VIN код: AAA-AAAAAA-A-AAAAAAA |
| `insuranceNumberRegExp` | Номер полиса страхования: ABC 1234567890 |
| `vehiclePassport` | Паспорт ТС: 12 AA 123456 |
| `phoneNumberRegExp` | Телефон: +7 (999) 999-99-99 |
| `serviceLicenseRegexp` | Лицензия сервиса: AAA-12-123456 |
| `driverPassportRegexp` | Номер паспорта водителя: 1234 123456 |
| `driverLicenseRegexp` | Номер прав водителя: 12 12 123456 |
| `decimalRegexp` | Десятичное число с одним знаком после запятой |
| `trimRegexp` | Обнаружение пробелов в начале/конце строки |
| `emailRegexp` | Email адрес |
| `nameRegexp` | Имя только с буквами и дефисом |
| `patronymicRegexp` | Отчество с буквами, пробелами и дефисом |

### ValidationRules.general

| Правило | Описание |
|---------|----------|
| `required` | Обязательное поле |
| `shortPassword` | Пароль минимум 8 символов |
| `alphaNum` | Буквы, цифры и подчеркивание |
| `alphaNumWithSpaces` | Буквы, цифры, пробелы и подчеркивание |
| `nameRule` | Только буквы |
| `patronymicRule` | Буквы, пробелы и дефисы |
| `minMaxLength(min, max)` | Ограничение длины |
| `email` | Валидация email формата |
| `onlyDigits` | Только цифры |
| `floatDecimal` | Число с одним знаком после запятой |
| `floatTwoSymbols` | Число с 1-2 знаками после запятой |
| `validationFloatingNumbers` | Только целые числа |
| `positiveNumbers` | Только положительные целые числа |
| `checkPhoneMask` | Формат +7 (999) 999-99-99 |
| `checkPhoneNumber` | Формат 8 (999) 999-99-99 |
| `maskRegEx` | Маска 12 символов |
| `maxLength(n)` | Максимальная длина n символов |
| `minLength(n)` | Минимальная длина n символов |
| `max(n)` | Максимальное значение n |
| `maxInt(name?)` | Максимальное значение с учетом типа поля |
| `minInt(name?)` | Минимальное значение с учетом типа поля |
| `passportNumberRule` | Формат паспорта 1234 123456 |
| `driverLicenseRule` | Формат прав 12 12 123456 |
| `serviceLicenseRule` | Формат лицензии AAA-12-123456 |
| `checkStateNumber` | Формат госномера |
| `checkSemitrailerNumber` | Формат госномера полуприцепа |
| `checkVin` | Формат VIN кода |
| `checkInsuranceNumber` | Формат полиса страхования |
| `checkVehiclePassport` | Формат паспорта ТС |
| `minManufactureYear(n)` | Минимальный год производства |
| `validatorStartDate(form, endDateKey, isSameDay)` | Валидация даты начала |
| `validatorEndDate(form, startDateKey, [diff, unit])` | Валидация даты окончания |
| `maxWaitingTime` | Время ожидания 0-500 минут |
| `maxLoadersTime` | Время работы грузчиков 500 минут |
| `checkTrimmedField` | Без пробелов в начале/конце |

---

## Прочие утилиты

Различные вспомогательные функции.

| Функция | Описание |
|---------|----------|
| `indexById(items)` | Создает объект-индекс по id из массива |
| `ioTypeFromEnum(name, enum)` | Создает io-ts тип из TypeScript enum |
| `_env(name)` | Получение значения переменной окружения |
| `jwtDecode(token)` | Декодирует JWT токен |
| `b64EncodeUnicode(str)` | Кодирует строку в base64 (поддержка UTF-8) |
| `b64DecodeUnicode(str)` | Декодирует base64 строку (поддержка UTF-8) |
| `isObject(value)` | Проверяет, является ли значение объектом |
| `isFilledObject(value)` | Проверяет, является ли значение непустым объектом |
| `isEmptyObject(value)` | Проверяет, является ли значение пустым объектом |
| `isObjectWithEmptyValues(value)` | Проверяет, содержит ли объект только пустые значения |
| `isEmptyArray(value)` | Проверяет, является ли значение пустым массивом |
| `isEqualArrays(arr1, arr2)` | Проверяет равенство двух массивов |
| `clearSymbols(value)` | Очищает строку от символов, кроме кириллицы, латиницы и цифр |
| `inputCleaner(event, form)` | Очистка и валидация input при вводе |
| `includesByLowerCaseAndSpaces(first, second)` | Проверяет вхождение подстроки без учета регистра |
| `startsWithIgnoreCase(source, target)` | Проверяет начало строки без учета регистра |
| `toWildcardRegexp(input)` | Преобразует wildcard-строку в регулярное выражение |
| `ignore()` | Функция-заглушка |
| `preventDefault(event)` | Предотвращает событие Enter в input |
| `deepMerge(source, target)` | Глубокое слияние двух объектов |
| `getErrorMessage(error)` | Получает сообщение об ошибке из ответа Axios |
| `getErrorProblems(error, restrict)` | Получает список проблем валидации из ответа Axios |
| `handlePlug(logger, type, description)` | Отображает сообщение через logger |
| `uuid()` | Генерирует случайный UUID версии 4 (RFC4122) |
| `matchPattern(pattern)` | Обработчик input, фильтрующий ввод по регулярному выражению |
| `phoneNumberRegexpInternational` | Регулярное выражение для международного формата телефона |
| `onlyNumbersRegExp` | Регулярное выражение только для цифр |
| `settingsPhoneNumber` | Настройки компонента PhoneNumberInput |

---

## Настройки

### settingsPhoneNumber

Настройки для компонента PhoneNumberInput:

```typescript
{
  input: {
    mask: '+7 (999) 999-99-99',
    placeholder: '+7 (___) ___-__-__',
  },
  clearPhone: (phone: string) => string,
  test: (phone: string) => boolean,
  warning: () => string,
}
```
