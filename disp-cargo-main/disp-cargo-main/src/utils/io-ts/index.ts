/* eslint-disable @typescript-eslint/no-explicit-any */
import * as t from 'io-ts';
import * as R from 'ramda';
import { either, orElse, chain } from 'fp-ts/lib/Either';
import moment from 'moment';
import { pipe } from 'fp-ts/lib/pipeable';

export * from './wrapped';

// eslint-disable-next-line @stylistic/max-len
export const optional = <T, U>(x: t.Type<T, U>): t.UnionC<[t.Type<T, U, unknown>, /* t.NullC, */ t.UndefinedC]> => t.union([x, /* t.null, */ t.undefined]);

export function withValidate<C extends t.Any>(codec: C, validate: C['validate'], name: string = codec.name): C {
  const r: C = {
    ...codec, validate, name, decode: (i: any) => validate(i, t.getDefaultContext(r)),
  };

  return r;
}

export function fallback<C extends t.Any>(codec: C, a: t.TypeOf<C>, name = `withFallback(${codec.name})`): C {
  return withValidate(codec, (u, c) => orElse(() => t.success(a))(codec.validate(u, c)), name);
}

export type NumberStringC = t.Type<number, string, unknown>;

export const numberString: NumberStringC = new t.Type<number, string, unknown>(
  'NumberFromString',
  t.number.is,
  (u, c) => either.chain(t.string.validate(u, c), s => {
    const n = +s;
    return Number.isNaN(n) || s.trim() === '' ? t.failure(u, c) : t.success(n);
  }),
  String
);

export type MoneyC = t.Type<number, number, unknown>;

/**
 * На бекенде суммы хранятся в виде целого числа в копейках,
 * соответственно, на фронте необходимо конвертировать данной число
 */
export const money: MoneyC = new t.Type<number, number, unknown>(
  'Money',
  t.number.is,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(s / 100)),
  n => Math.floor(n * 100)
);

export type MobilePhone = t.Type<string>;

export const mobilePhone: MobilePhone = new t.Type<string>(
  'MobilePhone',
  t.string.is,
  t.string.validate,
  n => `+${n.replace(/[^\d]/g, '')}`
);
export const nullable = <T, U>(x: t.Type<T, U>): t.UnionC<[t.Type<T, U, unknown>, t.NullC]> => t.union([x, t.null]);

export type EpochTimestampC = t.Type<Date, number, unknown>;

export const epochTimestamp: EpochTimestampC = new t.Type<Date, number, unknown>(
  'EpochTimestamp',
  (u): u is Date => u instanceof Date,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(new Date(s))),
  date => date.getTime()
);
export type TimeC = t.Type<number, number, unknown>;

export const time: TimeC = new t.Type<number, number, unknown>(
  'Time',
  t.number.is,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(s / 60000)),
  n => Math.floor(n * 60000)
);

export type ISODateC = t.Type<moment.Moment, string, unknown>;

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

export type EpochMSC = t.Type<moment.Moment, number, unknown>;

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

export type UUID = t.Branded<string, UUIDBrand>;

const uuidRe = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const validateUUID = (s: string): s is UUID => uuidRe.test(s);

export const uuid = t.brand(t.string, validateUUID, 'UUID');

type Literals<Variants extends [string, string, ...string[]]> = {
  [K in keyof Variants]: Variants[K] extends string ? t.LiteralC<Variants[K]> : never;
};

export const oneOf = <T extends string, Variants extends [T, T, ...T[]]>(
  ...variants: Variants
): t.UnionC<Literals<Variants>> => t.union(R.map(t.literal)(variants) as Literals<Variants>);

export type DecoderInput<T> = T extends t.Type<any, infer U> ? U : never;
