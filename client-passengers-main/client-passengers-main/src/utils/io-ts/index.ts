/* eslint-disable @typescript-eslint/no-explicit-any */
import { either, orElse } from 'fp-ts/lib/Either';
import * as t from 'io-ts';
import * as R from 'ramda';

export * from './wrapped';

export const optional = <T, U>(x: t.Type<T, U>): t.UnionC<[t.Type<T, U, unknown>, /* t.NullC, */ t.UndefinedC]> => t.union([x, /* t.null, */ t.undefined]);

export function withValidate<C extends t.Any>(codec: C, validate: C['validate'], name: string = codec.name): C {
  const r: C = {
    ...codec, validate, name, decode: (i: any) => validate(i, t.getDefaultContext(r)),
  };

  return r;
}

export const nullable = <T, U>(x: t.Type<T, U>): t.UnionC<[t.Type<T, U, unknown>, t.NullC]> => t.union([x, t.null]);

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

export const money: MoneyC = new t.Type<number, number, unknown>(
  'Money',
  t.number.is,
  (u, c) => either.chain(t.number.validate(u, c), s => t.success(s / 100)),
  n => Math.floor(n * 100)
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
  variants: Variants
): t.UnionC<Literals<Variants>> => t.union(R.map(t.literal)(variants) as Literals<Variants>);
