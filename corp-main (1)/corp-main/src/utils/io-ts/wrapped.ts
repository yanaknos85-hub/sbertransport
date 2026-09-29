import * as t from 'io-ts';
import { either as Either } from 'fp-ts';

export function wrapped<T, C>(typ: t.Type<T>, Cls: new (x: T) => C): t.Type<C, C, unknown> {
  return new t.Type<C, C, unknown>(
    Cls.name,
    (x): x is C => x instanceof Cls,
    (u, _c) => Either.map<T, C>(x => new Cls(x))(typ.decode(u)),
    c => ({ ...c })
  );
}
