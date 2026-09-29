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
 */
function b64EncodeUnicode(str: string): string {
  return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (match, p1) => String.fromCharCode(parseInt(p1, 16))));
}

/**
 * Декодирование строки из base64
 * @description корректно работает с UTF-8
 */
function b64DecodeUnicode(str: string): string {
  return decodeURIComponent(
    Array.prototype.map.call(atob(str), c => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`).join('')
  );
}

/**
 * Получение данных токена
 * @param token JWT токен авторизации
 * @returns декодированный набор данных токена
 */
function jwtDecode<T>(token: string): T {
  return JSON.parse(b64DecodeUnicode(token.split('.')[1]));
}

function isObject(value: unknown): value is Record<string, unknown> {
  return R.is(Object, value) && !Array.isArray(value);
}

function isFilledObject(value: unknown): boolean {
  return isObject(value) && Object.keys(value).length > 0;
}

function isEmptyObject(value: unknown): boolean {
  return isObject(value) && Object.keys(value).length === 0;
}

function isObjectWithEmptyValues(value: unknown): boolean {
  return isObject(value) && Object.values(value).every(item => item === null || item === undefined);
}

function isEmptyArray(value: unknown): boolean {
  return Array.isArray(value) && value.length === 0;
}

function isEqualArrays(arr1: (string | number)[], arr2: (string | number)[]): boolean {
  return arr1.length === arr2.length && arr1.every(item => arr2.includes(item));
}

/**
 * @description Удаление всех символов, кроме русского и английского языков и цифр
 */
function clearSymbols(value: string): string {
  return value.replace(/[^0-9A-Za-zА-Яа-я]/g, '');
}

/**
 * Функция проверки, не позволяет отправить пробелы в начале и конце строки минимально заданной длины,
 * предотвращает падение по ошибке при валидации на стороне бэка. Задать параметр name и повесить на onChange
 * обработчик валидируемого Input.
 * @param event
 * @param form
 */
const inputCleaner = (event: React.ChangeEvent<HTMLInputElement>, form: FormInstance): void => {
  const {
    value, minLength, name,
  } = event.target;
  const correctValue = value.replace(/[^a-zA-Z0-9а-яА-ЯЁё@. +,-]/gi, '').replace(/\s+/g, ' ');
  form.setFieldsValue({ [name]: correctValue.length === minLength ? correctValue.trim() : correctValue });
};

function includesByLowerCaseAndSpaces(first: string, second: string): boolean {
  const prepareFirst = clearSymbols(first);
  const prepareSecond = clearSymbols(second);

  return prepareFirst.toLowerCase().includes(prepareSecond.toLowerCase());
}

const startsWithIgnoreCase = (source: string, target: string): boolean => source.substr(0, target.length).localeCompare(target, undefined, { sensitivity: 'accent' }) === 0;

/**
 * @description Преобразует строку с wildcard (*) в соответствующее регулярное выражение
 * @param input исходная строка
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

const ignore = (): void => undefined;
const preventDefault = (e: React.KeyboardEvent<HTMLInputElement>): void => {
  if (e.key === 'Enter') {
    e.preventDefault();
  }
};
const phoneNumberRegexpInternational = /(\+7 \(\d{3}\) \d{3}-\d{2}-\d{2})|(\+7\d{10})/;
const onlyNumbersRegExp = /^[0-9]*$/;

function deepMerge<T>(source: Partial<T>, target: Partial<T>): T {
  return R.is(Object, source) && R.is(Object, target) ? R.mergeWith(deepMerge, source, target) : target;
}

const getErrorMessage = (error: AxiosError): string => error.response?.data?.message;

const handlePlug = (logger: ILogger, type: Notify, description: string): void => logger.toMessage(type, description);

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
  handlePlug,
  settingsPhoneNumber
};
