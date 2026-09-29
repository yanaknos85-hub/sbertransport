import { either } from 'fp-ts';
import * as t from 'io-ts';
import { formatValidationErrors } from 'io-ts-reporters';

import * as tt from '.';

const goodUUID = 'e8b47522-a7d5-487b-b74a-14e5186f23bb';
const badUUID = 'e8b47522-a7d5-487b-b74a-';

const formatErrors = either.mapLeft(formatValidationErrors);

const assertSuccess = <T>(x: either.Either<t.Errors, T>, a: T): void => expect(x).toEqual(either.right(a));

const assertFailure = (x: either.Either<t.Errors, any>, e: string[]): void => expect(formatErrors(x)).toEqual(either.left(e));

describe('Money', () => {
  test('Decodes successfully', () => assertSuccess(tt.money.decode(200), 2));

  test('Encodes successfully', () => expect(tt.money.encode(2.0)).toEqual(200));

  test('Decode fails on non-numbers', () => expect(formatErrors(tt.money.decode('200'))).toMatchObject(
    either.left(['Expecting Money but instead got: "200"'])
  ));
});

describe('UUID', () => {
  test('Decodes successfully', () => assertSuccess(tt.uuid.decode(goodUUID), goodUUID as tt.UUID));

  test('Fails on non-GUIDS', () => assertFailure(tt.uuid.decode(badUUID), [`Expecting UUID but instead got: "${badUUID}"`]));
});

class X {
  m: number;

  constructor({ money }: { money: number }) {
    this.m = money;
  }
}

const x = t.type({ money: tt.money });

describe('wrapped', () => {
  test('Creates an instance of class', () => assertSuccess(tt.wrapped(x, X).decode({ money: 200 }), new X({ money: 2 })));

  test('Works with arrays', () => assertSuccess(t.array(tt.wrapped(x, X)).decode([{ money: 200 }, { money: 250 }]), [
    new X({ money: 2 }),
    new X({ money: 2.5 }),
  ]));
});
