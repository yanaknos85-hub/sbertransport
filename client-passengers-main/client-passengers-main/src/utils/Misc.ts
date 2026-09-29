/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { AxiosError, AxiosResponse } from 'axios';
import isPlainObject from 'lodash/isPlainObject';
import moment from 'moment';

import { declOfNum, declOfNumForSymbols } from './declOfNum';
import { Sizes } from '../modules/EmployeeApp/types/Cargo';

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

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type, @typescript-eslint/no-empty-function
function noop() {}

function isFilledObject(value: any): boolean {
  return isPlainObject(value) && Object.keys(value).length > 0;
}

function isEmptyObject(value: any): boolean {
  return isPlainObject(value) && Object.keys(value).length === 0;
}

type ConstructorType = new (value: any, ...args: any) => any;

/**
 * @param TypeName Название модели(типа), в которую будет осуществлён каст
 * @param value Значение (массив или объект), который будет каститься
 * @param args Аргументы конструктора модели
 */
function plainToNew<R>(
  TypeName: ConstructorType,
  value: object | object[] | undefined,
  ...args: any
): typeof value extends undefined ? undefined : R {
  if (value instanceof TypeName || isEmptyObject(value) || (Array.isArray(value) && value.length === 0)) {
    return value as unknown as R;
  }

  // Если передаётся массив моделей, то вернём их без преобразований
  if (Array.isArray(value) && value[0] instanceof TypeName) {
    return value.map(x => x) as unknown as R;
  }

  if (Array.isArray(value) && isPlainObject(value[0])) {
    return value.map(x => new TypeName(x, ...args)) as unknown as R;
  }

  if (isFilledObject(value)) {
    return new TypeName(value, ...args) as R;
  }

  return undefined as any;
}

function joinUrl(...string: string[]): string {
  return string.join('').replace(/\/\/+/g, '/');
}

/**
 * @description Удаление всех символов, кроме русского и английского языков и цифр
 */
function clearSymbols(value: string): string {
  return value.replace(/[^0-9A-Za-zА-Яа-я.]/, '');
}

/**
 * @description Замена бесконечного числа пробелов на один
 */
function formatSpaces(value: string): string {
  return value.replace(/ +/g, ' ').trim();
}

function includesByLowerCaseAndSpaces(first?: string, second?: string): boolean {
  if (first && second) {
    const prepareSecond = clearSymbols(formatSpaces(second));

    return first.toLowerCase().includes(prepareSecond.toLowerCase());
  }

  return false;
}

const isAxiosResponse = (response: any): response is AxiosResponse => 'data' in response;
const isAxiosError = (error: any): error is AxiosError => 'isAxiosError' in error && 'response' in error;

export const formatPercents = (percent: number): string => new Intl.NumberFormat('ru-RU', {
  style: 'percent',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
}).format(percent);

/**
 * @value миллисекунды
 */
const getDaysTime = (value = 0) => {
  const days = Math.floor(value / (24 * 60 * 60 * 1000));
  const daysms = value % (24 * 60 * 60 * 1000);
  const hours = Math.floor(daysms / (60 * 60 * 1000));
  const hoursms = value % (60 * 60 * 1000);
  const minutes = Math.floor(hoursms / (60 * 1000));
  const minutesms = value % (60 * 1000);
  const sec = Math.floor(minutesms / 1000);

  return {
    days: days || 0,
    hours: hours || 0,
    minutes: minutes || 0,
    sec: sec || 0,
  };
};

/**
 * @value миллисекунды
 */
const getTime = (value = 0, showHours?: boolean, showMinutes?: boolean) => {
  const daysTime = getDaysTime(value);
  return [
    daysTime.days && `${daysTime.days} д`,
    daysTime.hours && (showHours || !daysTime.days) && `${daysTime.hours} ч`,
    daysTime.minutes && (showMinutes || !daysTime.hours) && `${daysTime.minutes} мин`,
  ]
    .filter(Boolean)
    .join('. ');
};

/**
 * @value километры
 */
const getDistance = (value = 0) => `${Math.round(value)} км`;

/**
 * @value килограммы
 */
const getWeight = (value = 0) => `${Math.round(value)} кг`;

/**
 * @value сантиметры
 */
const getVolume = (value = 0) => {
  const val = value / (100 * 100 * 100);
  return `${val.toString().split('.')[1]?.length > 2 ? val.toFixed(2) : val}  м³`;
};

const getTimeString = (timeValue: number): string => {
  const time = timeValue / 60000;
  if (time < 60) {
    return `${Math.round(time)} мин`;
  }
  return `${Math.trunc(time / 60)} ч ${Math.round(time % 60)} мин`;
};

export const getYear = (selectedMonth: number): number => {
  const isNewYear = selectedMonth === 0 && new Date().getMonth() !== 0;
  return isNewYear ? new Date().getFullYear() + 1 : new Date().getFullYear();
};

export const sortByTime = (array: any[]): any[] => array.sort((start, end) => moment(end.creationTime).valueOf() - moment(start.creationTime).valueOf());

const getDistanceString = (distance: number): string => `${distance.toFixed(2)} км`;

const ignore = (): void => undefined;

const getErrorMessage = (error?: AxiosError): string => {
  const response = JSON.parse(error?.request.response);
  return response.message;
};

export const toMillimeters = (size: number): number => size * 10;

export const sizesToUnits = <T extends Exclude<Sizes, 'volume' | 'weight'>>(obj: T): T => Object.entries(obj).reduce((acc: any, [key, value]: any) => {
  acc[key] = toMillimeters(value);
  return acc;
}, {});

const _cn = (classNames: (string | boolean | number | null | undefined)[]): string => `${classNames}`.replace(/,/g, ' ').trim();

const rootElement: () => Element = (): Element => (document as Document).querySelector('#root') as Element;

const phoneNumberRegexp = /\+7 \(\d{3}\) \d{3}-\d{2}-\d{2}|\+7\d{10}/;

export {
  phoneNumberRegexp,
  _env,
  jwtDecode,
  b64DecodeUnicode,
  b64EncodeUnicode,
  noop,
  plainToNew,
  isFilledObject,
  isEmptyObject,
  joinUrl,
  isAxiosResponse,
  isAxiosError,
  clearSymbols,
  formatSpaces,
  includesByLowerCaseAndSpaces,
  getTimeString,
  getDaysTime,
  getTime,
  getDistance,
  getWeight,
  getVolume,
  getDistanceString,
  ignore,
  getErrorMessage,
  declOfNum,
  declOfNumForSymbols,
  _cn,
  rootElement
};
