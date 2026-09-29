import * as R from 'ramda';

import { AxiosError } from 'axios';
import React from 'react';
import { FormInstance } from 'antd/lib/form/Form';
import { ILogger } from '@sber-sbertransport/mf-core';
import { Notify } from '@sber-sbertransport/mf-core/dist/types/stores/Logger/Logger.interface';

/**
 * Получение значения переменной окружения
 * @param name имя переменной окружения
 * @returns значение переменной окружения
 */
function _env(name: string): string | undefined {
  return process.env[`REACT_APP_${name}`] || process.env[name];
}

/**
 * Кодирование строки в base64
 * @description корректно работает с UTF-8
 * @param str строка для кодирования
 * @returns base64 строка
 */
function b64EncodeUnicode(str: string): string {
  return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (match, p1) => String.fromCharCode(parseInt(p1, 16))));
}

/**
 * Декодирование строки из base64
 * @description корректно работает с UTF-8
 * @param str base64 строка
 * @returns декодированная строка
 */
function b64DecodeUnicode(str: string): string {
  return decodeURIComponent(
    Array.prototype.map.call(atob(str), c => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`).join('')
  );
}

/**
 * Получение данных токена из JWT
 * @param token JWT токен авторизации
 * @returns декодированный набор данных токена
 */
function jwtDecode<T>(token: string): T {
  return JSON.parse(b64DecodeUnicode(token.split('.')[1]));
}

/**
 * Проверка, является ли значение объектом
 * @param value проверяемое значение
 * @returns true если значение является объектом
 */
function isObject(value: unknown): value is Record<string, unknown> {
  return R.is(Object, value) && !Array.isArray(value);
}

/**
 * Проверка, является ли значение заполненным объектом (не пустым)
 * @param value проверяемое значение
 * @returns true если значение является объектом и содержит хотя бы один ключ
 */
function isFilledObject(value: unknown): boolean {
  return isObject(value) && Object.keys(value).length > 0;
}

/**
 * Проверка, является ли значение пустым объектом
 * @param value проверяемое значение
 * @returns true если значение является объектом и не содержит ключей
 */
function isEmptyObject(value: unknown): boolean {
  return isObject(value) && Object.keys(value).length === 0;
}

/**
 * Проверка, является ли значение объектом со всеми пустыми значениями
 * @param value проверяемое значение
 * @returns true если значение является объектом и все его свойства равны null или undefined
 */
function isObjectWithEmptyValues(value: unknown): boolean {
  return isObject(value) && Object.values(value).every(item => item === null || item === undefined);
}

/**
 * Проверка, является ли значение пустым массивом
 * @param value проверяемое значение
 * @returns true если значение является пустым массивом
 */
function isEmptyArray(value: unknown): boolean {
  return Array.isArray(value) && value.length === 0;
}

/**
 * Проверка равенства двух массивов
 * @param arr1 первый массив
 * @param arr2 второй массив
 * @returns true если массивы равны по длине и содержат одинаковые элементы
 */
function isEqualArrays(arr1: (string | number)[], arr2: (string | number)[]): boolean {
  return arr1.length === arr2.length && arr1.every(item => arr2.includes(item));
}

/**
 * Очистка строки от всех символов, кроме русского и английского языков и цифр
 * @description Удаляет все символы, кроме русского и английского языков и цифр
 * @param value строка для очистки
 * @returns очищенная строка
 */
function clearSymbols(value: string): string {
  return value.replace(/[^0-9A-Za-zА-Яа-я]/g, '');
}

/**
 * Очистка и валидация input при вводе
 * Функция проверяет, не позволяет отправить пробелы в начале и конце строки минимально заданной длины,
 * предотвращает падение по ошибке при валидации на стороне бэка. Задать параметр name и повесить на onChange
 * обработчик валидируемого Input.
 * @param event событие изменения input
 * @param form форма ант design
 */
const inputCleaner = (event: React.ChangeEvent<HTMLInputElement>, form: FormInstance): void => {
  const {
    value, minLength, name,
  } = event.target;
  const correctValue = value.replace(/[^a-zA-Z0-9а-яА-ЯЁё@. +,-]/gi, '').replace(/\s+/g, ' ');
  form.setFieldsValue({ [name]: correctValue.length === minLength ? correctValue.trim() : correctValue });
};

/**
 * Проверка вхождения подстроки в строку без учета регистра и пробелов
 * @param first первая строка
 * @param second вторая строка
 * @returns true если вторая строка содержится в первой (без учета регистра и пробелов)
 */
function includesByLowerCaseAndSpaces(first: string, second: string): boolean {
  const prepareFirst = clearSymbols(first);
  const prepareSecond = clearSymbols(second);

  return prepareFirst.toLowerCase().includes(prepareSecond.toLowerCase());
}

/**
 * Проверка, начинается ли строка с заданной подстроки (без учета регистра)
 * @param source исходная строка
 * @param target подстрока для поиска
 * @returns true если строка начинается с заданной подстроки
 */
const startsWithIgnoreCase = (source: string, target: string): boolean => source.substr(0, target.length).localeCompare(target, undefined, { sensitivity: 'accent' }) === 0;

/**
 * Преобразует строку с wildcard (*) в соответствующее регулярное выражение
 * @description Заменяет символ * на .*, экранирует специальные символы регулярных выражений
 * @param input исходная строка
 * @returns регулярное выражение для匹配
 */
const toWildcardRegexp = (input: string): RegExp => {
  const escaped = input
    .replace(/[.+?^${}()|[\]\\]/g, '\\$&')
    .split('*')
    .join('.*');
  const hasWildcard = escaped.indexOf('*') !== -1;
  const withWildcard = `${hasWildcard ? '^' : '.*'}${escaped}${hasWildcard ? '$' : '.*'}`;
  return new RegExp(`^${withWildcard}`, 'i');
};

/**
 * Функция-заглушка, ничего не делает
 */
const ignore = (): void => undefined;

/**
 * Предотвращает событие Enter в input
 * @param event событие клавиатуры
 */
const preventDefault = (e: React.KeyboardEvent<HTMLInputElement>): void => {
  if (e.key === 'Enter') {
    e.preventDefault();
  }
};
const phoneNumberRegexpInternational = /^\+7\s\(\d{3}\)\s\d{3}-\d{2}-\d{2}$/;
const onlyNumbersRegExp = /^[0-9]*$/;

/**
 * Глубокое слияние двух объектов
 * @param source исходный объект
 * @param target целевой объект
 * @returns слитый объект
 */
function deepMerge<T>(source: Partial<T>, target: Partial<T>): T {
  return R.is(Object, source) && R.is(Object, target) ? R.mergeWith(deepMerge, source, target) : target;
}

/**
 * Получение сообщения об ошибке из ответа Axios
 * @param error объект ошибки Axios
 * @returns сообщение об ошибке
 */
const getErrorMessage = (error: AxiosError): string => error.response?.data?.message;

/**
 * Получение списка проблем валидации из ответа Axios
 * @param error объект ошибки Axios
 * @param restrict массив полей для фильтрации (опционально)
 * @returns массив проблем валидации
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const getErrorProblems = (error: AxiosError, restrict?: string[]): any[] => {
  try {
    const problems = error.response?.data?.problems || [];
    return problems.filter(problem => !restrict || restrict.includes(problem.field));
  } catch {
    return [];
  }
};

/**
 * Отображение сообщения через logger
 * @param logger объект логгера
 * @param type тип сообщения
 * @param description текст сообщения
 */
const handlePlug = (logger: ILogger, type: Notify, description: string): void => logger.toMessage(type, description);

/**
 * Настройки для компонента PhoneNumberInput
 */
const settingsPhoneNumber = {
  input: {
    mask: '+7 (999) 999-99-99',
    placeholder: '+7 (___) ___-__-__',
  },
  clearPhone: (phone: string): string => phone?.replace(/[^+\d]/g, '') ?? '',
  test: (phone: string): boolean => /(\+7|8) \(\d{3}\) \d{3}-\d{2}-\d{2}/.test(phone),
  warning(): string {
    return `Телефон должен соответствовать формату ${this.input.mask}`;
  },
};

export {
  _env,
  jwtDecode,
  b64DecodeUnicode,
  b64EncodeUnicode,
  isObject,
  isFilledObject,
  isEmptyObject,
  isObjectWithEmptyValues,
  isEmptyArray,
  isEqualArrays,
  clearSymbols,
  inputCleaner,
  includesByLowerCaseAndSpaces,
  startsWithIgnoreCase,
  toWildcardRegexp,
  ignore,
  preventDefault,
  deepMerge,
  phoneNumberRegexpInternational,
  onlyNumbersRegExp,
  getErrorMessage,
  getErrorProblems,
  handlePlug,
  settingsPhoneNumber
};
