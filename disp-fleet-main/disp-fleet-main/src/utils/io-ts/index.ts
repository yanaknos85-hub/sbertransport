/* eslint-disable @typescript-eslint/no-explicit-any */
import * as t from 'io-ts';
import * as R from 'ramda';
import { either, orElse, chain } from 'fp-ts/lib/Either';
import moment from 'moment';
import { pipe } from 'fp-ts/lib/pipeable';

export * from './wrapped';

/**
 * Создает тип, который может быть либо T, либо undefined
 * @param x - базовый тип
 * @returns union тип T | undefined
 */
export const optional = <T, U>(x: t.Type<T, U>): t.UnionC<[t.Type<T, U, unknown>, /* t.NullC, */ t.UndefinedC]> => (
  t.union([x, /* t.null, */ t.undefined])
);

/**
 * Добавляет пользовательскую валидацию к существующему codec
 * @param codec - исходный codec
 * @param validate - функция валидации
 * @param name - имя типа для отладки
 * @returns новый codec с валидацией
 */
export function withValidate<C extends t.Any>(codec: C, validate: C['validate'], name: string = codec.name): C {
  const r: C = {
    ...codec, validate, name, decode: (i: any) => validate(i, t.getDefaultContext(r)),
  };

  return r;
}

/**
 * Добавляет fallback значение при ошибке валидации
 * @param codec - исходный codec
 * @param a - значение по умолчанию
 * @param name - имя типа для отладки
 * @returns новый codec с fallback значением
 */
export function fallback<C extends t.Any>(codec: C, a: t.TypeOf<C>, name = `withFallback(${codec.name})`): C {
  return withValidate(codec, (u, c) => orElse(() => t.success(a))(codec.validate(u, c)), name);
}

export type NumberStringC = t.Type<number, string, unknown>;

/**
 * Декодирует строку в число (если возможно)
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
 * Декодирует деньги из копеек в рубли (делит на 100)
 */
export type MoneyC = t.Type<number, number, unknown>;

/**
 * На бекенде суммы хранятся в виде целого числа в копейках,
 * соответственно, на фронте необходимо конвертировать данное число
 * Декодирует деньги из копеек в рубли (делит на 100), кодирует в обратную сторону
 */
export const money: MoneyC = new t.Type<number, number, unknown>(
  'Money',
  t.number.is,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(s / 100)),
  n => Math.floor(n * 100)
);

/**
 * Декодирует номер телефона и добавляет код страны +7
 */
export type MobilePhone = t.Type<string>;

/**
 * Декодирует номер телефона и добавляет код страны +7, кодирует в обратную сторону (только цифры)
 */
export const mobilePhone: MobilePhone = new t.Type<string>(
  'MobilePhone',
  t.string.is,
  t.string.validate,
  n => `+${n.replace(/[^\d]/g, '')}`
);

/**
 * Создает тип, который может быть либо T, либо null
 * @param x - базовый тип
 * @returns union тип T | null
 */
export const nullable = <T, U>(x: t.Type<T, U>): t.UnionC<[t.Type<T, U, unknown>, t.NullC]> => t.union([x, t.null]);

/**
 * Декодирует timestamp в Date объект, кодирует в timestamp
 */
export type EpochTimestampC = t.Type<Date, number, unknown>;

export const epochTimestamp: EpochTimestampC = new t.Type<Date, number, unknown>(
  'EpochTimestamp',
  (u): u is Date => u instanceof Date,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(new Date(s))),
  date => date.getTime()
);

/**
 * Декодирует время в минутах (мс / 60000), кодирует в мс
 */
export type TimeC = t.Type<number, number, unknown>;

export const time: TimeC = new t.Type<number, number, unknown>(
  'Time',
  t.number.is,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(s / 60000)),
  n => Math.floor(n * 60000)
);

/**
 * Декодирует ISODate в Moment объект, кодирует в ISO строку
 */
export type ISODateC = t.Type<moment.Moment, string, unknown>;

/**
 * Декодирует ISODate в Moment объект, кодирует в ISO строку
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
 * Декодирует timestamp (мс) в Moment объект, кодирует в timestamp
 */
export type EpochMSC = t.Type<moment.Moment, number, unknown>;

/**
 * Декодирует timestamp (мс) в Moment объект, кодирует в timestamp
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

interface UUIDBrand {
  readonly UUID: unique symbol;
}

/**
 * UUID строка с брендингом для типобезопасности
 */
export type UUID = t.Branded<string, UUIDBrand>;

/**
 * Валидирует строку как UUID (формат RFC4122)
 * @param s - строка для валидации
 * @returns true если строка валидный UUID
 */
const uuidRe = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/**
 * Валидирует строку как UUID (формат RFC4122)
 * @param s - строка для валидации
 * @returns true если строка валидный UUID
 */
const validateUUID = (s: string): s is UUID => uuidRe.test(s);

/**
 * Декодирует строку в UUID тип
 */
export const uuid = t.brand(t.string, validateUUID, 'UUID');

type Literals<Variants extends [string, string, ...string[]]> = {
  [K in keyof Variants]: Variants[K] extends string ? t.LiteralC<Variants[K]> : never;
};

/**
 * Создает union из литеральных типов
 * @param variants - варианты литералов
 * @returns union тип из литералов
 */
export const oneOf = <T extends string, Variants extends [T, T, ...T[]]>(
  ...variants: Variants
): t.UnionC<Literals<Variants>> => t.union(R.map(t.literal)(variants) as Literals<Variants>);

/**
 * Тип входа для декодера io-ts
 */
export type DecoderInput<T> = T extends t.Type<any, infer U> ? U : never;
