/* eslint-disable @typescript-eslint/no-explicit-any */
import * as t from 'io-ts';
import * as R from 'ramda';
import { either, orElse, chain } from 'fp-ts/lib/Either';
import moment from 'moment';
import { pipe } from 'fp-ts/lib/pipeable';

export * from './wrapped';

/**
 * Создает тип, который принимает либо значение типа T, либо null/undefined
 * @param x - io-ts type для оборачивания
 * @returns Union тип (T | undefined)
 */
export const optional = <T, U>(
  x: t.Type<T, U>
): t.UnionC<[t.Type<T, U, unknown>, /* t.NullC, */ t.UndefinedC]> => t.union([x, /* t.null, */ t.undefined]);

/**
 * Добавляет кастомную валидацию к существующему io-ts типу
 * @param codec - Исходный io-ts тип
 * @param validate - Функция валидации
 * @param name - Имя типа (по умолчанию имя исходного типа)
 * @returns Новый io-ts тип с кастомной валидацией
 */
export function withValidate<C extends t.Any>(codec: C, validate: C['validate'], name: string = codec.name): C {
  const r: C = {
    ...codec, validate, name, decode: (i: any) => validate(i, t.getDefaultContext(r)),
  };

  return r;
}

/**
 * Создает тип с значением по умолчанию при ошибке валидации
 * @param codec - Исходный io-ts тип
 * @param a - Значение по умолчанию
 * @param name - Имя типа (по умолчанию withFallback(...))
 * @returns Новый io-ts тип с fallback значением
 */
export function fallback<C extends t.Any>(codec: C, a: t.TypeOf<C>, name = `withFallback(${codec.name})`): C {
  return withValidate(codec, (u, c) => orElse(() => t.success(a))(codec.validate(u, c)), name);
}

/**
 * Тип для строки, которая преобразуется в число (например, из JSON API)
 * Валидирует, что строка содержит корректное числовое значение
 */
export type NumberStringC = t.Type<number, string, unknown>;

/**
 * io-ts тип для преобразования строки в число
 * Пример: "123" → 123
 */
export const numberString: NumberStringC = new t.Type<number, string, unknown>(
  'NumberFromString',
  t.number.is,
  (u, c) => either.chain(t.string.validate(u, c), s => {
    const n = +s;
    return Number.isNaN(n) || s.trim() === '' ? t.failure(u, c) : t.success(n);
  }),
  String
);

/**
 * Тип для денежных значений в рублях (конвертирует копейки в рубли)
 * На бэкенде суммы хранятся в копейках, на фронте — в рублях
 */
export type MoneyC = t.Type<number, number, unknown>;

/**
 * io-ts тип для денежных значений
 * Конвертирует копейки в рубли при декодировании: 1000 → 10
 * При кодировании возвращает копейки: 10 → 1000
 */
export const money: MoneyC = new t.Type<number, number, unknown>(
  'Money',
  t.number.is,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(s / 100)),
  n => Math.floor(n * 100)
);

/**
 * Тип для мобильного телефона
 * Валидирует строку телефона и форматирует для отправки на бэкенд
 */
export type MobilePhone = t.Type<string>;

/**
 * io-ts тип для мобильного телефона
 * Валидирует строку телефона, возвращает отформатированный номер
 */
export const mobilePhone: MobilePhone = new t.Type<string>(
  'MobilePhone',
  t.string.is,
  (u, c) => either.chain(t.string.validate(u, c), s => {
    const formatted = s.replace(/[^\d+]/g, '');
    // If string starts with +, keep it; otherwise add it
    return t.success(formatted.startsWith('+') ? formatted : `+${formatted}`);
  }),
  n => `+${n.replace(/[^\d]/g, '')}`
);

/**
 * Создает тип, который принимает либо значение типа T, либо null
 * @param x - io-ts type для оборачивания
 * @returns Union тип (T | null)
 */
export const nullable = <T, U>(x: t.Type<T, U>): t.UnionC<[t.Type<T, U, unknown>, t.NullC]> => t.union([x, t.null]);

/**
 * Тип для временных меток (timestamp) в миллисекундах, преобразуемых в Date
 */
export type EpochTimestampC = t.Type<Date, number, unknown>;

/**
 * io-ts тип для timestamp
 * Преобразует число (milliseconds) в Date объект
 */
export const epochTimestamp: EpochTimestampC = new t.Type<Date, number, unknown>(
  'EpochTimestamp',
  (u): u is Date => u instanceof Date,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(new Date(s))),
  date => date.getTime()
);

/**
 * Тип для времени в минутах (хранится как число)
 */
export type TimeC = t.Type<number, number, unknown>;

/**
 * io-ts тип для времени
 * Преобразует миллисекунды в минуты: 60000 → 1
 */
export const time: TimeC = new t.Type<number, number, unknown>(
  'Time',
  t.number.is,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(s / 60000)),
  n => Math.floor(n * 60000)
);

/**
 * Тип для даты с использованием moment.js
 */
export type ISODateC = t.Type<moment.Moment, string, unknown>;

/**
 * io-ts тип для ISO даты
 * Валидирует и парсит ISO строку в moment.Moment объект
 */
export const ISODate: ISODateC = new t.Type<moment.Moment, string, unknown>(
  'ISODate',
  (u): u is moment.Moment => moment.isMoment(u),
  (u, c) => pipe(
    t.string.validate(u, c),
    chain(s => {
      const d = moment(s);
      return Number.isNaN(d.date()) ? t.failure(u, c) : t.success(d);
    })
  ),
  a => a.toISOString()
);

/**
 * Тип для даты из milliseconds в moment.Moment
 */
export type EpochMSC = t.Type<moment.Moment, number, unknown>;

/**
 * io-ts тип для даты из milliseconds
 */
export const EpochMS: EpochMSC = new t.Type<moment.Moment, number, unknown>(
  'EpochMS',
  (u): u is moment.Moment => moment.isMoment(u),
  (u, c) => pipe(
    t.number.validate(u, c),
    chain(s => {
      const d = moment(s);
      return Number.isNaN(d.date()) ? t.failure(u, c) : t.success(d);
    })
  ),
  a => a.valueOf()
);

/**
 * Брендированный тип для UUID
 */
interface UUIDBrand {
  readonly UUID: unique symbol;
}

/**
 * Тип для UUID строки с брендированием
 */
export type UUID = t.Branded<string, UUIDBrand>;

/**
 * Регулярное выражение для валидации UUID v4
 */
const uuidRe = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/**
 * Валидирует строку на соответствие UUID v4 формату
 * @param s - Строка для валидации
 * @returns true если строка соответствует UUID формату
 */
const validateUUID = (s: string): s is UUID => uuidRe.test(s);

/**
 * io-ts тип для UUID v4
 * Валидирует соответствие формату UUID
 */
export const uuid = t.brand(t.string, validateUUID, 'UUID');

type Literals<Variants extends [string, string, ...string[]]> = {
  [K in keyof Variants]: Variants[K] extends string ? t.LiteralC<Variants[K]> : never;
};

/**
 * Создает union тип из списка литералов
 * @param variants - Массив возможных строковых значений
 * @returns Union тип из literal типов
 */
export const oneOf = <T extends string, Variants extends [T, T, ...T[]]>(
  ...variants: Variants
): t.UnionC<Literals<Variants>> => t.union(R.map(t.literal)(variants) as Literals<Variants>);

/**
 * Тип для декодера входных данных io-ts
 */
export type DecoderInput<T> = T extends t.Type<any, infer U> ? U : never;
